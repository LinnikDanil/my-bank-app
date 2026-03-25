package ru.practicum.account.integration.notification.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.account.integration.notification.client.ApiClient;

@Configuration
public class NotificationRestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder notificationRestClientBuilder() {
        return ApiClient.buildRestClientBuilder();
    }
}
