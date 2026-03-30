package ru.practicum.cash.integration.account.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.cash.integration.account.client.ApiClient;
import ru.practicum.common.integration.security.OAuth2ClientCredentialsInterceptor;
@Configuration
public class AccountRestClientConfig {
    @Bean
    public RestClient.Builder accountRestClientBuilder(
            OAuth2ClientCredentialsInterceptor oAuth2ClientCredentialsInterceptor,
            AccountRestClientLoggingInterceptor loggingInterceptor
    ) {
        return ApiClient.buildRestClientBuilder()
                .requestInterceptor(oAuth2ClientCredentialsInterceptor)
                .requestInterceptor(loggingInterceptor);
    }
}
