package ru.practicum.front.integration;

import lombok.RequiredArgsConstructor;
import io.micrometer.observation.ObservationRegistry;
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
    private final ObservationRegistry observationRegistry;

    @Bean
    public RestClient.Builder frontRestClientBuilder() {
        return RestClient.builder()
                .observationRegistry(observationRegistry);
    }

    @Bean
    public AccountApi accountApi(RestClient.Builder frontRestClientBuilder) {
        var apiClient = new ru.practicum.front.integration.account.client.ApiClient(frontRestClientBuilder.clone().build());
        apiClient.setBasePath(properties.accountBaseUrl());
        apiClient.setBearerToken(bearerTokenSupplier::get);
        return new AccountApi(apiClient);
    }

    @Bean
    public CashApi cashApi(RestClient.Builder frontRestClientBuilder) {
        var apiClient = new ru.practicum.front.integration.cash.client.ApiClient(frontRestClientBuilder.clone().build());
        apiClient.setBasePath(properties.cashBaseUrl());
        apiClient.setBearerToken(bearerTokenSupplier::get);
        return new CashApi(apiClient);
    }

    @Bean
    public TransferApi transferApi(RestClient.Builder frontRestClientBuilder) {
        var apiClient = new ru.practicum.front.integration.transfer.client.ApiClient(frontRestClientBuilder.clone().build());
        apiClient.setBasePath(properties.transferBaseUrl());
        apiClient.setBearerToken(bearerTokenSupplier::get);
        return new TransferApi(apiClient);
    }
}
