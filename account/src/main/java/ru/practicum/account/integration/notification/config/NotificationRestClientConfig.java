package ru.practicum.account.integration.notification.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.practicum.account.integration.notification.client.ApiClient;
import ru.practicum.common.integration.security.OAuth2ClientCredentialsInterceptor;
/**
 * Конфигурация {@link RestClient} для интеграции с Notification-сервисом.
 */
@Configuration
public class NotificationRestClientConfig {
    /**
     * Строит RestClient.Builder для интеграции с Notification-сервисом.
     */
    @Bean
    public RestClient.Builder notificationRestClientBuilder(
            OAuth2ClientCredentialsInterceptor oAuth2ClientCredentialsInterceptor,
            NotificationRestClientLoggingInterceptor loggingInterceptor) {
        return ApiClient.buildRestClientBuilder()
                .requestInterceptor(oAuth2ClientCredentialsInterceptor)
                .requestInterceptor(loggingInterceptor);
    }
}
