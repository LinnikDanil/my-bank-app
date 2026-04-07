package ru.practicum.cash.integration.account.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.cash.integration.account.api.AccountInternalApi;
import ru.practicum.cash.integration.account.client.ApiClient;

@Configuration
@EnableConfigurationProperties(AccountIntegrationProperties.class)
public class AccountClientConfig {

    @Bean
    public AccountInternalApi accountInternalApi(AccountIntegrationProperties properties,
                                                 RestClient accountRestClient) {
        ApiClient apiClient = new ApiClient(accountRestClient);
        apiClient.setBasePath(properties.baseUrl());
        return new AccountInternalApi(apiClient);
    }
}
