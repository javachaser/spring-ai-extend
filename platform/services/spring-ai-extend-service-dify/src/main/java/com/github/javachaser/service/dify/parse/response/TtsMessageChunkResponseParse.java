package com.github.javachaser.service.dify.parse.response;

import com.github.javachaser.service.dify.api.DifyAiChatApi.ChunkChatCompletionResponse;
import com.github.javachaser.service.dify.api.DifyAiChatApi.TtsMessageChunkResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;
import java.util.Map;

/**
 * @author 85949
 * @date 2025/5/9 13:54
 * @description
 */
public class TtsMessageChunkResponseParse implements ChunkResponseParse {

    @Override
    public boolean support(ChunkChatCompletionResponse response) {
        return response instanceof TtsMessageChunkResponse;
    }

    @Override
    public ChatResponse parse(ChunkChatCompletionResponse response) {
        TtsMessageChunkResponse chatCompletion = (TtsMessageChunkResponse) response;

        // @formatter:off
        Map<String, Object> metadata = Map.of(
                "event",chatCompletion.event(),
                "task_id",chatCompletion.taskId(),
                "message_id",chatCompletion.messageId(),
                "created_at",chatCompletion.createdAt()
        );
        // @formatter:on
        var assistantMessage = new AssistantMessage(chatCompletion.audio(), metadata);
        List<Generation> generations = List.of(new Generation(assistantMessage));
        return new ChatResponse(generations);
    }

}
