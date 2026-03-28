package ru.practicum.cash.integration.notification.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.cash.integration.notification.client.ApiClient;
import ru.practicum.common.integration.security.OAuth2ClientCredentialsInterceptor;

@Configuration
public class NotificationRestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder notificationRestClientBuilder(
            OAuth2ClientCredentialsInterceptor oAuth2ClientCredentialsInterceptor,
            NotificationRestClientLoggingInterceptor loggingInterceptor
    ) {
        return ApiClient.buildRestClientBuilder()
                .requestInterceptor(oAuth2ClientCredentialsInterceptor)
                .requestInterceptor(loggingInterceptor);
    }
}
