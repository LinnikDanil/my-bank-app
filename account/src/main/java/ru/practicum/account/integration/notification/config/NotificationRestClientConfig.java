package ru.practicum.account.integration.notification.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.account.integration.notification.client.ApiClient;

/**
 * Конфигурация {@link RestClient} для интеграции с Notification-сервисом.
 */
@Configuration
public class NotificationRestClientConfig {

    /**
     * Строит load-balanced RestClient.Builder:
     * имя хоста в baseUrl может быть service-id из Service Discovery.
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder notificationRestClientBuilder(
            NotificationRestClientLoggingInterceptor loggingInterceptor) {
        return ApiClient.buildRestClientBuilder()
                .requestInterceptor(loggingInterceptor);
    }
}
