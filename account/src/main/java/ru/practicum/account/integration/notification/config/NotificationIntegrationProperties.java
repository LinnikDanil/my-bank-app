package ru.practicum.account.integration.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Параметры интеграции с Notification-сервисом.
 */
@ConfigurationProperties(prefix = "integration.notification")
public record NotificationIntegrationProperties(
        String baseUrl
) {
}
