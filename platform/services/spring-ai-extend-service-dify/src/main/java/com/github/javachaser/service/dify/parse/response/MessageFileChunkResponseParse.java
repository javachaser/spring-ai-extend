package com.github.javachaser.service.dify.parse.response;

import com.github.javachaser.service.dify.api.DifyAiChatApi.ChunkChatCompletionResponse;
import com.github.javachaser.service.dify.api.DifyAiChatApi.MessageFileChunkResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;
import java.util.Map;

/**
 * @author 85949
 * @date 2025/5/9 13:53
 * @description
 */
public class MessageFileChunkResponseParse implements ChunkResponseParse {

    @Override
    public boolean support(ChunkChatCompletionResponse response) {
        return response instanceof MessageFileChunkResponse;
    }

    @Override
    public ChatResponse parse(ChunkChatCompletionResponse response) {
        MessageFileChunkResponse chatCompletion = (MessageFileChunkResponse) response;

        // @formatter:off
        Map<String, Object> metadata = Map.of(
                "event",chatCompletion.event(),
                "id",chatCompletion.id(),
                "type",chatCompletion.type(),
                "belongs_to",chatCompletion.belongsTo(),
                "conversation_id",chatCompletion.conversationId()
        );
        // @formatter:on
        var assistantMessage = new AssistantMessage(chatCompletion.url(), metadata);
        List<Generation> generations = List.of(new Generation(assistantMessage));
        return new ChatResponse(generations);
    }
}
