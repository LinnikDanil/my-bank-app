package ru.practicum.front.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "front.gateway")
public record GatewayClientProperties(String baseUrl) {
}
