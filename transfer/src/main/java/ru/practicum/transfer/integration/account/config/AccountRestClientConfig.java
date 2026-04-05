package ru.practicum.transfer.integration.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.common.integration.security.OAuth2ClientCredentialsInterceptor;

@Configuration
public class AccountRestClientConfig {

    @Bean
    public RestClient.Builder accountRestClientBuilder(
            RestClient.Builder restClientBuilder,
            OAuth2ClientCredentialsInterceptor oAuth2ClientCredentialsInterceptor,
            AccountRestClientLoggingInterceptor loggingInterceptor
    ) {
        return restClientBuilder.clone()
                .requestInterceptor(oAuth2ClientCredentialsInterceptor)
                .requestInterceptor(loggingInterceptor);
    }
}
