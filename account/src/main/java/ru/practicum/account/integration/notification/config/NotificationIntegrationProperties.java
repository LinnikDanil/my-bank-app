package ru.practicum.account.integration.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.notification")
public record NotificationIntegrationProperties(
        String baseUrl
) {
}
