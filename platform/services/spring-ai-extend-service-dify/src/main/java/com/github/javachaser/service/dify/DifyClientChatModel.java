package com.github.javachaser.service.dify;

import com.github.javachaser.constants.CommonValue;
import com.github.javachaser.service.dify.api.DifyAiChatApi;
import com.github.javachaser.service.dify.api.DifyAiChatApi.ChatCompletionRequest;
import com.github.javachaser.service.dify.api.DifyAiChatApi.ChatCompletionResponse;
import com.github.javachaser.service.dify.api.DifyAiChatApi.ChunkChatCompletionResponse;
import com.github.javachaser.service.dify.api.DifyApiConstants;
import com.github.javachaser.service.dify.metadata.DifyUsage;
import com.github.javachaser.service.dify.options.DifyAiChatOptions;
import com.github.javachaser.service.dify.options.FileDomain;
import com.github.javachaser.service.dify.parse.response.ResponseParseManager;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author javachaser
 * @date 2024/1/10 15:58
 */
public class DifyClientChatModel implements ChatModel, StreamingChatModel {

    private static final Logger logger = LoggerFactory.getLogger(DifyClientChatModel.class);

    private static final ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultChatModelObservationConvention();

    /**
     * The retry template used to retry the ZhiPuAI API calls.
     */
    public final RetryTemplate retryTemplate;

    /**
     * The default options used for the chat completion requests.
     */
    private final DifyAiChatOptions defaultOptions;

    /**
     * Low-level access to the DifyAiApi API.
     */
    private final DifyAiChatApi difyAiChatApi;

    /**
     * Observation registry used for instrumentation.
     */
    private final ObservationRegistry observationRegistry;

    /**
     * Conventions to use for generating observations.
     */
    private ChatModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;


    public DifyClientChatModel(DifyAiChatApi difyAiChatApi) {
        this(difyAiChatApi, DifyAiChatOptions.builder().build());
    }

    public DifyClientChatModel(DifyAiChatApi difyAiChatApi, DifyAiChatOptions options) {
        this(difyAiChatApi, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public DifyClientChatModel(DifyAiChatApi difyAiChatApi, DifyAiChatOptions options, RetryTemplate retryTemplate) {
        this(difyAiChatApi, options, retryTemplate, ObservationRegistry.NOOP);
    }

    /**
     * Initializes a new instance of the DifyClientChatModel.
     *
     * @param difyAiChatApi       The DifyAiApi instance to be used for interacting with the DifyAiApi Chat API.
     * @param options             The DifyAiChatOptions to configure the chat model.
     * @param retryTemplate       The retry template.
     * @param observationRegistry The ObservationRegistry used for instrumentation.
     */
    public DifyClientChatModel(DifyAiChatApi difyAiChatApi, DifyAiChatOptions options,
                               RetryTemplate retryTemplate, ObservationRegistry observationRegistry) {
        Assert.notNull(difyAiChatApi, "ZhiPuAiApi must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        Assert.notNull(observationRegistry, "ObservationRegistry must not be null");
        this.retryTemplate = retryTemplate;
        this.defaultOptions = options;
        this.difyAiChatApi = difyAiChatApi;
        this.observationRegistry = observationRegistry;
    }


    private ChatResponseMetadata from(ChatCompletionResponse result) {
        Assert.notNull(result, "ChatResponse must not be null");
        return ChatResponseMetadata.builder()
                .id(result.id())
                .usage(DifyUsage.from(result.metadata().usage()))
                .model("")
                .build();
    }


    @Override
    public ChatResponse call(Prompt prompt) {

        ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                .prompt(prompt)
                .provider(DifyApiConstants.PROVIDER_NAME)
                .build();

        return ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
                .observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                        this.observationRegistry)
                .observe(() -> {
                    ChatCompletionRequest request = createRequest(prompt, false);

                    ResponseEntity<ChatCompletionResponse> completionEntity = this.retryTemplate
                            .execute(ctx -> this.difyAiChatApi.chatCompletionEntity(request));

                    var chatCompletion = completionEntity.getBody();

                    if (chatCompletion == null) {
                        logger.warn("No chat completion returned for prompt: {}", prompt);
                        return new ChatResponse(List.of());
                    }

                    // @formatter:off
                    Map<String, Object> metadata = Map.of(
                            "id", chatCompletion.id(),
                            "task_id",chatCompletion.taskId(),
                            "message_id",chatCompletion.messageId()
                    );
                    // @formatter:on
                    var assistantMessage = new AssistantMessage(chatCompletion.answer(), metadata);
                    List<Generation> generations = List.of(new Generation(assistantMessage));
                    ChatResponse chatResponse = new ChatResponse(generations, from(completionEntity.getBody()));
                    observationContext.setResponse(chatResponse);
                    return chatResponse;
                });
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return DifyAiChatOptions.fromOptions(this.defaultOptions);
    }


    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        Assert.notNull(prompt, "Prompt must not be null");
        Assert.isTrue(!CollectionUtils.isEmpty(prompt.getInstructions()), "Prompt messages must not be empty");

        return Flux.deferContextual(contextView -> {
            ChatCompletionRequest request = createRequest(prompt, true);

            Flux<ChunkChatCompletionResponse> completionChunks = this.retryTemplate
                    .execute(ctx -> this.difyAiChatApi.chatCompletionStream(request));

            // For chunked responses, only the first chunk contains the choice role.
            // The rest of the chunks with same ID share the same role.
            ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

            ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
                    .prompt(prompt)
                    .provider(DifyApiConstants.PROVIDER_NAME)
                    .build();

            Observation observation = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION.observation(
                    this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                    this.observationRegistry);

            observation.parentObservation(contextView.getOrDefault(ObservationThreadLocalAccessor.KEY, null)).start();

            // Convert the ChatCompletionChunk into a ChatCompletion to be able to reuse
            // the function call handling logic.
            Flux<ChatResponse> chatResponse = completionChunks.switchMap(chatCompletion ->
                    Mono.just(chatCompletion).map(chunkChatCompletion -> {
                                try {
                                    return ResponseParseManager.parse(chunkChatCompletion);
                                } catch (Exception e) {
                                    logger.error("Error processing chat completion", e);
                                    return new ChatResponse(List.of());
                                }
                            }
                    )
            );

            return new MessageAggregator().aggregate(chatResponse, observationContext::setResponse);
        });
    }

    /**
     * Accessible for testing.
     */
    ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {

        DifyAiChatOptions options = defaultOptions;
        if (prompt.getOptions() != null) {
            options = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class, DifyAiChatOptions.class);
        }

        String user = Optional.ofNullable(options.getUser()).orElse(UUID.randomUUID().toString());
        String conversationId = options.getConversationId();
        List<FileDomain> files = options.getFiles();
        Boolean autoGenerateName = options.getAutoGenerateName();

        return prompt.getInstructions()
                .stream()
                .filter(message -> message.getMessageType().equals(MessageType.USER))
                .findFirst()
                .map(message -> {
                    String text = message.getText();
                    Map<String, Object> metadata = message.getMetadata();
                    String type;
                    if (stream) {
                        type = CommonValue.ResponseMode.STREAMING.getType();
                    } else {
                        type = CommonValue.ResponseMode.BLOCKING.getType();
                    }
                    return new ChatCompletionRequest(text, metadata, type, user, conversationId, files, autoGenerateName);
                })
                .orElseThrow(() -> new IllegalArgumentException("当前消息内容解析失败"));
    }


    /**
     * Use the provided convention for reporting observation data
     *
     * @param observationConvention The provided convention
     */
    public void setObservationConvention(ChatModelObservationConvention observationConvention) {
        Assert.notNull(observationConvention, "observationConvention cannot be null");
        this.observationConvention = observationConvention;
    }
}
