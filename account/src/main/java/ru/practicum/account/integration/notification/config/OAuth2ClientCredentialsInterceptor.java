package ru.practicum.account.integration.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2ClientCredentialsInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String registrationId;
    private final Authentication principal;

    public OAuth2ClientCredentialsInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${integration.oauth2.registration-id}") String registrationId
    ) {
        this.authorizedClientManager = authorizedClientManager;
        this.registrationId = registrationId;
        this.principal = new AnonymousAuthenticationToken(
                "account-service-key",
                "account-service",
                AuthorityUtils.createAuthorityList("ROLE_SYSTEM")
        );
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        OAuth2AuthorizedClient authorizedClient = authorizeClient();
        request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
        return execution.execute(request, body);
    }

    private OAuth2AuthorizedClient authorizeClient() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(principal)
                .build();
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            throw new IllegalStateException(
                    "Failed to obtain access token via client_credentials for registrationId=" + registrationId
            );
        }
        return authorizedClient;
    }
}
