package com.github.javachaser.starter.dify;

import com.github.javachaser.service.dify.DifyClientChatModel;
import com.github.javachaser.service.dify.api.DifyAiChatApi;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author 85949
 * @date 2025/5/9 16:58
 * @description
 */
@Configuration
@ConditionalOnClass(DifyAiChatApi.class)
@AutoConfiguration(after = {
        RestClientAutoConfiguration.class,
        WebClientAutoConfiguration.class,
        SpringAiRetryAutoConfiguration.class})
@ImportAutoConfiguration(classes = {
        SpringAiRetryAutoConfiguration.class,
        RestClientAutoConfiguration.class,
        WebClientAutoConfiguration.class
})
@EnableConfigurationProperties({
        DifyConnectionProperties.class,
        DifyChatProperties.class
})
public class DifyAutoConfiguration {

    @ConditionalOnProperty(prefix = DifyChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    protected static class DifyChatConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public DifyClientChatModel difyClientChatModel(
                DifyConnectionProperties commonProperties,
                DifyChatProperties chatProperties,
                RestClient.Builder restClientBuilder,
                WebClient.Builder webClientBuilder,
                RetryTemplate retryTemplate,
                ResponseErrorHandler responseErrorHandler,
                ObjectProvider<ObservationRegistry> observationRegistry,
                ObjectProvider<ChatModelObservationConvention> observationConvention
        ) {
            var dashscopeApi = new DifyAiChatApi(
                    chatProperties.getBaseUrl(),
                    chatProperties.getApiKey(),
                    restClientBuilder,
                    webClientBuilder,
                    responseErrorHandler
            );
            var dashscopeModel = new DifyClientChatModel(
                    dashscopeApi,
                    chatProperties.getOptions(),
                    retryTemplate,
                    observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)
            );
            observationConvention.ifAvailable(dashscopeModel::setObservationConvention);
            return dashscopeModel;
        }
    }
}
