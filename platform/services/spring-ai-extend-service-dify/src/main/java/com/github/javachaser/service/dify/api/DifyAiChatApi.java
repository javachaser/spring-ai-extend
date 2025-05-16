package com.github.javachaser.service.dify.api;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javachaser.constants.CommonValue;
import com.github.javachaser.service.dify.options.FileDomain;
import lombok.Getter;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Dify调用类
 *
 * @author 85949
 * @date 2025/4/30 15:37
 * @description
 */
public class DifyAiChatApi {

    private final RestClient restClient;

    private final WebClient webClient;

    public DifyAiChatApi(String difyApiKey) {
        this(DifyApiConstants.DEFAULT_BASE_URL, difyApiKey);
    }

    public DifyAiChatApi(String baseUrl, String difyApiKey) {
        this(baseUrl, difyApiKey, RestClient.builder(), WebClient.builder(), RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
    }

    public DifyAiChatApi(String difyApiKey, RestClient.Builder restClientBuilder,
                         WebClient.Builder webClientBuilder, ResponseErrorHandler responseErrorHandler) {
        this(DifyApiConstants.DEFAULT_BASE_URL, difyApiKey, restClientBuilder, webClientBuilder, responseErrorHandler);
    }

    public DifyAiChatApi(String baseUrl, String difyApiKey, RestClient.Builder restClientBuilder,
                         WebClient.Builder webClientBuilder, ResponseErrorHandler responseErrorHandler) {

        Consumer<HttpHeaders> authHeaders = h -> {
            h.setBearerAuth(difyApiKey);
            h.setContentType(MediaType.APPLICATION_JSON);
        };

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeaders(authHeaders)
                .defaultStatusHandler(responseErrorHandler)
                .build();

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeaders(authHeaders)
                .build();
    }


    public ResponseEntity<ChatCompletionResponse> chatCompletionEntity(ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(CommonValue.ResponseMode.BLOCKING.isEq(chatRequest.responseMode), "Request must set the responseMode property to blocking.");

        return this.restClient.post()
                .uri("/chat-messages")
                .body(chatRequest)
                .retrieve()
                .toEntity(ChatCompletionResponse.class);
    }


    public Flux<ChunkChatCompletionResponse> chatCompletionStream(ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(CommonValue.ResponseMode.STREAMING.isEq(chatRequest.responseMode), "Request must set the responseMode property to streaming.");

        return this.webClient.post()
                .uri("/chat-messages")
                .body(Mono.just(chatRequest), ChatCompletionRequest.class)
                .retrieve()
                .bodyToFlux(String.class)
                .handle((data, sink) -> {
                    ChunkChatCompletionResponse response = ModelOptionsUtils.jsonToObject(data, BaseChunkChatCompletionResponse.class);
                    ChunkResponseEventType eventType = ChunkResponseEventType.parse(response.event());
                    ChunkChatCompletionResponse result = switch (eventType) {
                        case MESSAGE -> ModelOptionsUtils.jsonToObject(data, MessageChunkResponse.class);
                        case AGENT_THOUGHT -> ModelOptionsUtils.jsonToObject(data, AgentThoughtChunkResponse.class);
                        case MESSAGE_FILE -> ModelOptionsUtils.jsonToObject(data, MessageFileChunkResponse.class);
                        case MESSAGE_END -> ModelOptionsUtils.jsonToObject(data, MessageEndChunkResponse.class);
                        case TTS_MESSAGE -> ModelOptionsUtils.jsonToObject(data, TtsMessageChunkResponse.class);
                        case ERROR -> ModelOptionsUtils.jsonToObject(data, ErrorChunkResponse.class);
                        case PING -> ModelOptionsUtils.jsonToObject(data, PingChunkResponse.class);
                    };
                    sink.next(result);
                });
    }

    @JsonInclude(Include.NON_NULL)
    public record ChatCompletionResponse(
            @JsonProperty("event") String event,
            @JsonProperty("task_id") String taskId,
            @JsonProperty("id") String id,
            @JsonProperty("message_id") String messageId,
            @JsonProperty("conversation_id") String conversationId,
            @JsonProperty("mode") String mode,
            @JsonProperty("answer") String answer,
            @JsonProperty("metadata") Metadata metadata,
            @JsonProperty("created_at ") Long createdAt
    ) {

    }

    @JsonInclude(Include.NON_NULL)
    public record Metadata(
            @JsonProperty("usage") TokenUsage usage,
            @JsonProperty("retriever_resources") List<RetrieverResource> retrieverResources
    ) {
    }

    @JsonInclude(Include.NON_NULL)
    public record TokenUsage(
            @JsonProperty("prompt_tokens") Integer promptTokens,
            @JsonProperty("prompt_unit_price") String promptUnitPrice,
            @JsonProperty("prompt_price_unit") String promptPriceUnit,
            @JsonProperty("prompt_price") String promptPrice,
            @JsonProperty("completion_tokens") Integer completionTokens,
            @JsonProperty("completion_unit_price") String completionUnitPrice,
            @JsonProperty("completion_price_unit") String completionPriceUnit,
            @JsonProperty("completion_price") String completionPrice,
            @JsonProperty("total_tokens") Integer totalTokens,
            @JsonProperty("total_price") String totalPrice,
            @JsonProperty("currency") String currency,
            @JsonProperty("latency") BigDecimal latency
    ) {
    }

    @JsonInclude(Include.NON_NULL)
    public record RetrieverResource(
            @JsonProperty("position") Long position,
            @JsonProperty("dataset_id") String dataset_id,
            @JsonProperty("dataset_name") String dataset_name,
            @JsonProperty("document_id") String document_id,
            @JsonProperty("document_name") String document_name,
            @JsonProperty("segment_id") String segment_id,
            @JsonProperty("score") BigDecimal score,
            @JsonProperty("content") String content
    ) {
    }

    @JsonInclude(Include.NON_NULL)
    public record ChatCompletionRequest(
            @JsonProperty("query") String query,
            @JsonProperty("inputs") Map<String, Object> inputs,
            @JsonProperty("response_mode") String responseMode,
            @JsonProperty("user") String user,
            @JsonProperty("conversation_id") String conversationId,
            @JsonProperty("files") List<FileDomain> files,
            @JsonProperty("auto_generate_name") Boolean autoGenerateName) {
    }

    public interface ChunkChatCompletionResponse {
        String event();
    }

    public record BaseChunkChatCompletionResponse(
            @JsonProperty("event") String event
    ) implements ChunkChatCompletionResponse {
    }


    @JsonInclude(Include.NON_NULL)
    public record MessageChunkResponse(
            @JsonProperty("event") String event,
            @JsonProperty("task_id") String taskId,
            @JsonProperty("message_id") String messageId,
            @JsonProperty("conversation_id") String conversationId,
            @JsonProperty("answer") String answer,
            @JsonProperty("created_at") Integer createdAt
    ) implements ChunkChatCompletionResponse {
    }

    /**
     * 仅Agent模式下使用
     */
    public record AgentThoughtChunkResponse(
            @JsonProperty("event") String event,
            @JsonProperty("id") String id,
            @JsonProperty("task_id") String taskId,
            @JsonProperty("message_id") String messageId,
            @JsonProperty("position") Integer position,
            @JsonProperty("thought") String thought,
            @JsonProperty("observation") String observation,
            @JsonProperty("tool") String tool,
            //  json字符串
            @JsonProperty("tool_input") String toolInput,
            @JsonProperty("created_at") Integer createdAt,
            @JsonProperty("message_files") List<String> messageFiles,
            @JsonProperty("conversation_id") String conversationId
    ) implements ChunkChatCompletionResponse {
    }

    public record MessageFileChunkResponse(
            @JsonProperty("event") String event,
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("belongs_to") String belongsTo,
            @JsonProperty("url") String url,
            @JsonProperty("conversation_id") String conversationId
    ) implements ChunkChatCompletionResponse {
    }

    public record MessageEndChunkResponse(
            @JsonProperty("event") String event,
            @JsonProperty("task_id") String taskId,
            @JsonProperty("message_id") String messageId,
            @JsonProperty("conversation_id") String conversationId,
            @JsonProperty("metadata") Metadata metadata
    ) implements ChunkChatCompletionResponse {
    }

    public record TtsMessageChunkResponse(
            @JsonProperty("event") String event,
            @JsonProperty("task_id") String taskId,
            @JsonProperty("message_id") String messageId,
            // audio 语音合成之后的音频块使用 Base64 编码之后的文本内容，播放的时候直接 base64 解码送入播放器即可
            @JsonProperty("audio") String audio,
            @JsonProperty("created_at") Integer createdAt
    ) implements ChunkChatCompletionResponse {
    }

    public record ErrorChunkResponse(
            @JsonProperty("event") String event,
            @JsonProperty("task_id") String taskId,
            @JsonProperty("message_id") String messageId,
            @JsonProperty("status") Integer status,
            @JsonProperty("code") String code,
            @JsonProperty("message") String message
    ) implements ChunkChatCompletionResponse {
    }

    public record PingChunkResponse(@JsonProperty("event") String event) implements ChunkChatCompletionResponse {
    }

    @Getter
    public enum ChunkResponseEventType {
        MESSAGE("message", "agent_message", "message_replace"),
        AGENT_THOUGHT("agent_thought"),
        MESSAGE_FILE("message_file"),
        MESSAGE_END("message_end"),
        TTS_MESSAGE("tts_message", "tts_message_end"),
        ERROR("error"),
        PING("ping");
        private final String[] type;

        ChunkResponseEventType(String... type) {
            this.type = type;
        }

        public static ChunkResponseEventType parse(String origin) {
            return Arrays.stream(values())
                    .filter(types -> Arrays.asList(types.getType()).contains(origin))
                    .findFirst()
                    .orElse(null);
        }
    }

}
