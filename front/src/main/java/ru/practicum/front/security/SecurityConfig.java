package ru.practicum.front.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Конфигурация безопасности фронта.
 *
 * <p>Все пользовательские страницы доступны только после OAuth2/OIDC-логина.
 * Выход выполняется как локально (Spring Security), так и на стороне Keycloak
 * через redirect на end-session endpoint.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Основная цепочка фильтров безопасности для front-app.
     *
     * <p>Разрешаем только технические endpoints (health/info/error), всё остальное
     * требует аутентификацию пользователя.</p>
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            LogoutSuccessHandler keycloakLogoutSuccessHandler) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health/**", "/actuator/info/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessHandler(keycloakLogoutSuccessHandler)
                        .permitAll()
                )
                .build();
    }

    /**
     * Обработчик успешного logout.
     *
     * <p>После локального logout перенаправляет пользователя в Keycloak logout endpoint,
     * чтобы завершить SSO-сессию (в т.ч. при "remember me"), и возвращает обратно на "/".</p>
     */
    @Bean
    LogoutSuccessHandler keycloakLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            String postLogoutRedirectUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/")
                    .build()
                    .toUriString();

            if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
                response.sendRedirect(postLogoutRedirectUri);
                return;
            }

            ClientRegistration registration = clientRegistrationRepository
                    .findByRegistrationId(oauth2Token.getAuthorizedClientRegistrationId());
            if (registration == null) {
                response.sendRedirect(postLogoutRedirectUri);
                return;
            }

            String authorizationUri = registration.getProviderDetails().getAuthorizationUri();
            if (authorizationUri == null || authorizationUri.isBlank()) {
                response.sendRedirect(postLogoutRedirectUri);
                return;
            }
            // В Spring ClientRegistration хранится auth endpoint, а для логаута нужен end-session endpoint.
            String logoutEndpoint = authorizationUri.replace(
                    "/protocol/openid-connect/auth",
                    "/protocol/openid-connect/logout"
            );

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(logoutEndpoint)
                    .queryParam("post_logout_redirect_uri", postLogoutRedirectUri)
                    .queryParam("client_id", registration.getClientId());
            Object principal = oauth2Token.getPrincipal();
            String idToken = null;
            if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser
                    && oidcUser.getIdToken() != null) {
                idToken = oidcUser.getIdToken().getTokenValue();
            }
            // id_token_hint помогает провайдеру точно завершить нужную пользовательскую сессию.
            if (idToken != null && !idToken.isBlank()) {
                builder.queryParam("id_token_hint", idToken);
            }

            response.sendRedirect(builder.build(true).toUriString());
        };
    }
}
