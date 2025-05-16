package com.github.javachaser.service.dify.parse.response;

import com.github.javachaser.service.dify.api.DifyAiChatApi.ErrorChunkResponse;
import com.github.javachaser.service.dify.api.DifyAiChatApi.ChunkChatCompletionResponse;
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
public class ErrorChunkResponseParse implements ChunkResponseParse {

    @Override
    public boolean support(ChunkChatCompletionResponse response) {
        return response instanceof ErrorChunkResponse;
    }

    @Override
    public ChatResponse parse(ChunkChatCompletionResponse response) {

        ErrorChunkResponse chatCompletion = (ErrorChunkResponse) response;

        // @formatter:off
        Map<String, Object> metadata = Map.of(
                "event",chatCompletion.event(),
                "task_id",chatCompletion.taskId(),
                "message_id",chatCompletion.messageId(),
                "status",chatCompletion.status(),
                "code",chatCompletion.code(),
                "created_at",chatCompletion.message()
        );
        // @formatter:on
        var assistantMessage = new AssistantMessage(chatCompletion.message(), metadata);
        List<Generation> generations = List.of(new Generation(assistantMessage));
        return new ChatResponse(generations);
    }

}
