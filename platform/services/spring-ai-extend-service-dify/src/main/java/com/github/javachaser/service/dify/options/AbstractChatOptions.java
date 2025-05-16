package com.github.javachaser.service.dify.options;

import org.springframework.ai.chat.prompt.ChatOptions;

import java.util.List;

/**
 * @author 85949
 * @date 2025/5/6 16:51
 * @description
 */

public abstract class AbstractChatOptions implements ChatOptions {

    @Override
    public String getModel() {
        return "";
    }

    @Override
    public Double getFrequencyPenalty() {
        return 0.0;
    }

    @Override
    public Integer getMaxTokens() {
        return 0;
    }

    @Override
    public Double getPresencePenalty() {
        return 0.0;
    }

    @Override
    public List<String> getStopSequences() {
        return List.of();
    }

    @Override
    public Double getTemperature() {
        return 0.0;
    }

    @Override
    public Integer getTopK() {
        return 0;
    }

    @Override
    public Double getTopP() {
        return 0.0;
    }

}
