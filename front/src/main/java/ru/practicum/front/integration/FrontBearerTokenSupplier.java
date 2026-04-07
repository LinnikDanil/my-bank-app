package ru.practicum.front.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FrontBearerTokenSupplier {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public String get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            throw new IllegalStateException("OAuth2 authentication is required");
        }

        OAuth2AuthorizedClient client = authorizedClientManager.authorize(
                OAuth2AuthorizeRequest.withClientRegistrationId(oauth2Token.getAuthorizedClientRegistrationId())
                        .principal(oauth2Token)
                        .build()
        );

        if (client == null || client.getAccessToken() == null || client.getAccessToken().getTokenValue() == null) {
            throw new IllegalStateException("Access token is missing");
        }
        return client.getAccessToken().getTokenValue();
    }
}
