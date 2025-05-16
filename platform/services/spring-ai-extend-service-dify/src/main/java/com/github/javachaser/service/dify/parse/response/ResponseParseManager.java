package com.github.javachaser.service.dify.parse.response;

import com.github.javachaser.service.dify.api.DifyAiChatApi.ChunkChatCompletionResponse;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;
import java.util.Optional;

/**
 * @author 85949
 * @date 2025/5/9 14:29
 * @description
 */
public class ResponseParseManager {


    private static final List<ChunkResponseParse> PARSE = List.of(
            new AgentThoughtChunkResponseParse(),
            new ErrorChunkResponseParse(),
            new MessageChunkResponseParse(),
            new MessageEndChunkResponseParse(),
            new MessageFileChunkResponseParse(),
            new PingChunkResponseParse(),
            new TtsMessageChunkResponseParse()
    );

    public static ChatResponse parse(ChunkChatCompletionResponse response) {
        return parse(response, true);
    }

    public static ChatResponse parse(ChunkChatCompletionResponse response, boolean throwExcept) {
        Optional<ChatResponse> optional = PARSE.stream()
                .filter(parse -> parse.support(response))
                .map(parse -> parse.parse(response))
                .findFirst();
        if (throwExcept) {
            return optional.orElseThrow(() -> new IllegalArgumentException("暂不支持当前类型消息"));
        } else {
            return optional.orElse(null);
        }
    }
}
