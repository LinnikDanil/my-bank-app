package ru.practicum.transfer.integration.account.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.account")
public record AccountIntegrationProperties(String baseUrl) {
}
