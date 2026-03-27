package ru.practicum.transfer.integration.account.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.transfer.integration.account.client.ApiClient;

@Configuration
public class AccountRestClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder accountRestClientBuilder(AccountRestClientLoggingInterceptor loggingInterceptor) {
        return ApiClient.buildRestClientBuilder()
                .requestInterceptor(loggingInterceptor);
    }
}
