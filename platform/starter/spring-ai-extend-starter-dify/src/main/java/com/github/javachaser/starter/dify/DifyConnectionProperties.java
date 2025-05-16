package com.github.javachaser.starter.dify;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.github.javachaser.service.dify.api.DifyApiConstants.DEFAULT_BASE_URL;
import static com.github.javachaser.service.dify.api.DifyApiConstants.DEFAULT_READ_TIMEOUT;

/**
 * @author 85949
 * @date 2025/5/12 10:52
 * @description
 */
@ConfigurationProperties(prefix = DifyConnectionProperties.CONFIG_PREFIX)
public class DifyConnectionProperties extends DifyProperties {
    public static final String CONFIG_PREFIX = "spring.ai.dify";

    private Integer readTimeout;

    public DifyConnectionProperties() {
        super.setBaseUrl(DEFAULT_BASE_URL);
        readTimeout = DEFAULT_READ_TIMEOUT;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }
}
