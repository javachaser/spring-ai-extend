package com.github.javachaser.starter.dify;

import com.github.javachaser.service.dify.options.DifyAiChatOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author 85949
 * @date 2025/5/12 10:58
 * @description
 */
@ConfigurationProperties(prefix = DifyChatProperties.CONFIG_PREFIX)
public class DifyChatProperties extends DifyProperties {

    /**
     * Dify configuration prefix.
     */
    public static final String CONFIG_PREFIX = "spring.ai.dify.chat";
    public static final Boolean DEFAULT_AUTO_GENERATE_NAME = false;

    @NestedConfigurationProperty
    private DifyAiChatOptions options = DifyAiChatOptions.builder()
            .autoGenerateName(DEFAULT_AUTO_GENERATE_NAME)
            .build();

    public DifyAiChatOptions getOptions() {
        return options;
    }

    public void setOptions(DifyAiChatOptions options) {
        this.options = options;
    }
}
