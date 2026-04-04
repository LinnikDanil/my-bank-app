package ru.practicum.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import ru.practicum.common.integration.security.OAuth2ClientCredentialsInterceptor;

@AutoConfiguration
public class CommonSecurityAutoConfiguration {

    @Bean
    @ConditionalOnBean(OAuth2AuthorizedClientManager.class)
    @ConditionalOnProperty(name = "integration.oauth2.registration-id")
    @ConditionalOnMissingBean
    public OAuth2ClientCredentialsInterceptor oAuth2ClientCredentialsInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${integration.oauth2.registration-id}") String registrationId,
            @Value("${spring.application.name}") String applicationName
    ) {
        return new OAuth2ClientCredentialsInterceptor(authorizedClientManager, registrationId, applicationName);
    }
}
