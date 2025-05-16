package com.github.javachaser.observation.conventions;

import org.springframework.ai.observation.conventions.AiProvider;

/**
 * Extend ApiProvider{@link AiProvider}
 *
 * @author javachaser
 * @date 2024/1/10 15:58
 */
public enum ExtendAiProvider {
    /**
     * AI platform provided by Dify.
     */
    DIFY("dify"),
    ;
    private final String value;

    ExtendAiProvider(String value) {
        this.value = value;
    }

    /**
     * Return the value of the provider.
     *
     * @return the value of the provider
     */
    public String value() {
        return this.value;
    }

    // @formatter:on

}
