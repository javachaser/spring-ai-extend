package com.github.javachaser.service.dify.parse.response;

import com.github.javachaser.service.dify.api.DifyAiChatApi.ChunkChatCompletionResponse;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * @author 85949
 * @date 2025/5/9 13:54
 * @description
 */
public interface ChunkResponseParse {

    boolean support(ChunkChatCompletionResponse response);


    ChatResponse parse(ChunkChatCompletionResponse response);
}
