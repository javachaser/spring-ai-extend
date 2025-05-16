package com.github.javachaser.service.dify.metadata;

import com.github.javachaser.service.dify.api.DifyAiChatApi.TokenUsage;
import org.springframework.ai.chat.metadata.Usage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 85949
 * @date 2025/5/9 10:38
 * @description
 */
public class DifyUsage implements Usage {

    private final TokenUsage usage;

    public DifyUsage(TokenUsage usage) {
        this.usage = usage;
    }

    public static DifyUsage from(TokenUsage usage) {
        return new DifyUsage(usage);
    }

    @Override
    public Integer getPromptTokens() {
        return usage.promptTokens();
    }

    @Override
    public Integer getCompletionTokens() {
        return usage.completionTokens();
    }

    @Override
    public Object getNativeUsage() {
        Map<String, Integer> usage = new HashMap<>();
        usage.put("promptTokens", getPromptTokens());
        usage.put("completionTokens", getCompletionTokens());
        usage.put("totalTokens", getTotalTokens());
        return usage;
    }

    @Override
    public Integer getTotalTokens() {
        return usage.totalTokens();
    }
}
