package ru.practicum.account.integration.notification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.account.integration.notification.api.NotificationInternalApi;
import ru.practicum.account.integration.notification.client.ApiClient;

@Configuration
@EnableConfigurationProperties(NotificationIntegrationProperties.class)
public class NotificationClientConfig {

    @Bean
    public NotificationInternalApi notificationInternalApi(NotificationIntegrationProperties properties,
                                                           RestClient.Builder notificationRestClientBuilder) {
        ApiClient apiClient = new ApiClient(notificationRestClientBuilder.build());
        apiClient.setBasePath(properties.baseUrl());
        return new NotificationInternalApi(apiClient);
    }
}
