package com.github.javachaser.service.dify.parse.response;

import com.github.javachaser.service.dify.api.DifyAiChatApi.ChunkChatCompletionResponse;
import com.github.javachaser.service.dify.api.DifyAiChatApi.AgentThoughtChunkResponse;
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
public class AgentThoughtChunkResponseParse implements ChunkResponseParse {

    @Override
    public boolean support(ChunkChatCompletionResponse response) {
        return response instanceof AgentThoughtChunkResponse;
    }

    @Override
    public ChatResponse parse(ChunkChatCompletionResponse response) {
        AgentThoughtChunkResponse chatCompletion = (AgentThoughtChunkResponse) response;

        // @formatter:off
        Map<String, Object> metadata = Map.of(
                "event",chatCompletion.event(),
                "task_id",chatCompletion.taskId(),
                "message_id",chatCompletion.messageId(),
                "position",chatCompletion.position(),
                "thought",chatCompletion.thought(),
                "tool",chatCompletion.tool(),
                "tool_input",chatCompletion.toolInput(),
                "created_at",chatCompletion.createdAt(),
                "message_files",chatCompletion.messageFiles(),
                "conversation_id",chatCompletion.conversationId()
        );
        // @formatter:on
        var assistantMessage = new AssistantMessage(chatCompletion.observation(), metadata);
        List<Generation> generations = List.of(new Generation(assistantMessage));
        return new ChatResponse(generations);
    }
}
