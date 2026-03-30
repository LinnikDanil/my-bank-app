package ru.practicum.front.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "front.api")
public record ApiClientProperties(
        String accountBaseUrl,
        String cashBaseUrl,
        String transferBaseUrl
) {
}
