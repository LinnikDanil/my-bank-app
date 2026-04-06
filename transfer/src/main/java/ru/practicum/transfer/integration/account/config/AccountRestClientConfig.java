package ru.practicum.transfer.integration.account.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.common.integration.security.OAuth2ClientCredentialsInterceptor;

@Configuration
public class AccountRestClientConfig {

    @Bean
    public RestClient accountRestClient(
            ObservationRegistry observationRegistry,
            OAuth2ClientCredentialsInterceptor oAuth2ClientCredentialsInterceptor,
            AccountRestClientLoggingInterceptor loggingInterceptor
    ) {
        return RestClient.builder()
                .observationRegistry(observationRegistry)
                .requestInterceptor(oAuth2ClientCredentialsInterceptor)
                .requestInterceptor(loggingInterceptor)
                .build();
    }
}
