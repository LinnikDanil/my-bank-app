package ru.practicum.front.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.front.integration.account.api.AccountApi;
import ru.practicum.front.integration.cash.api.CashApi;
import ru.practicum.front.integration.transfer.api.TransferApi;

@Configuration
@RequiredArgsConstructor
public class FrontApiClientConfig {

    private final ApiClientProperties properties;
    private final FrontBearerTokenSupplier bearerTokenSupplier;

    @Bean
    public AccountApi accountApi(RestClient.Builder restClientBuilder) {
        var apiClient = new ru.practicum.front.integration.account.client.ApiClient(restClientBuilder.clone().build());
        apiClient.setBasePath(properties.accountBaseUrl());
        apiClient.setBearerToken(bearerTokenSupplier::get);
        return new AccountApi(apiClient);
    }

    @Bean
    public CashApi cashApi(RestClient.Builder restClientBuilder) {
        var apiClient = new ru.practicum.front.integration.cash.client.ApiClient(restClientBuilder.clone().build());
        apiClient.setBasePath(properties.cashBaseUrl());
        apiClient.setBearerToken(bearerTokenSupplier::get);
        return new CashApi(apiClient);
    }

    @Bean
    public TransferApi transferApi(RestClient.Builder restClientBuilder) {
        var apiClient = new ru.practicum.front.integration.transfer.client.ApiClient(restClientBuilder.clone().build());
        apiClient.setBasePath(properties.transferBaseUrl());
        apiClient.setBearerToken(bearerTokenSupplier::get);
        return new TransferApi(apiClient);
    }
}
