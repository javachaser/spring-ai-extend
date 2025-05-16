package com.github.javachaser.service.dify.api;

import com.github.javachaser.observation.conventions.ExtendAiProvider;

/**
 * @author 85949
 * @date 2025/4/30 15:26
 * @description
 */
public final class DifyApiConstants {


    public static final String DEFAULT_BASE_URL = "https://api.dify.ai/v1";

    public static final Integer DEFAULT_READ_TIMEOUT = 60;

    public static final String PROVIDER_NAME = ExtendAiProvider.DIFY.value();

    private DifyApiConstants() {

    }

}
