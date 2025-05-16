package com.github.javachaser.service.dify.parse.response;

import com.github.javachaser.service.dify.api.DifyAiChatApi;
import com.github.javachaser.service.dify.api.DifyAiChatApi.MessageEndChunkResponse;
import com.github.javachaser.service.dify.metadata.DifyUsage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author 85949
 * @date 2025/5/9 13:53
 * @description
 */
public class MessageEndChunkResponseParse implements ChunkResponseParse {

    private ChatResponseMetadata from(MessageEndChunkResponse result) {
        Assert.notNull(result, "ChatResponse must not be null");
        return ChatResponseMetadata.builder()
                .id(result.messageId())
                .usage(DifyUsage.from(result.metadata().usage()))
                .model("")
                .build();
    }

    @Override
    public boolean support(DifyAiChatApi.ChunkChatCompletionResponse response) {
        return response instanceof MessageEndChunkResponse;
    }

    @Override
    public ChatResponse parse(DifyAiChatApi.ChunkChatCompletionResponse response) {
        MessageEndChunkResponse chatCompletion = (MessageEndChunkResponse) response;

        // @formatter:off
        Map<String, Object> metadata = Map.of(
                "event",chatCompletion.event(),
                "task_id",chatCompletion.taskId(),
                "message_id",chatCompletion.messageId(),
                "conversation_id",chatCompletion.conversationId(),
                "metadata",chatCompletion.metadata()
        );
        // @formatter:on

        var assistantMessage = new AssistantMessage(null, metadata);
        List<Generation> generations = List.of(new Generation(assistantMessage));
        return new ChatResponse(generations, from(chatCompletion));
    }
}
