package ru.practicum.account.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Конфигурация безопасности Account-сервиса как OAuth2 Resource Server.
 *
 * <p>Сервис принимает JWT от Keycloak, извлекает authorities из claims
 * {@code realm_access.roles} и {@code resource_access.*.roles},
 * а также направляет security-исключения в общий обработчик ошибок.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Настраивает фильтры Spring Security:
     * аутентификация по JWT и авторизация для всех endpoint, кроме health-check.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AuthenticationEntryPoint authenticationEntryPoint,
                                            AccessDeniedHandler accessDeniedHandler) {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Делегирует ошибки аутентификации в {@link org.springframework.web.bind.annotation.RestControllerAdvice},
     * чтобы вернуть единый формат ErrorResponse.
     */
    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        return (request, response, authException) -> resolver.resolveException(request, response, null, authException);
    }

    /**
     * Делегирует ошибки авторизации (403) в общий exception resolver.
     */
    @Bean
    AccessDeniedHandler accessDeniedHandler(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        return (request, response, accessDeniedException) ->
                resolver.resolveException(request, response, null, accessDeniedException);
    }

    /**
     * Конвертер JWT -> Authentication, где authorities берутся из ролей Keycloak.
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    /**
     * Объединяет роли realm и client в единый набор authorities без дублей.
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        roles.addAll(extractRealmRoles(jwt));
        roles.addAll(extractResourceRoles(jwt));

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    /**
     * Извлекает роли realm из claim {@code realm_access.roles}.
     */
    private List<String> extractRealmRoles(Jwt jwt) {
        Object realmAccessObj = jwt.getClaim("realm_access");
        if (!(realmAccessObj instanceof Map<?, ?> realmAccess)) {
            return List.of();
        }
        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof Collection<?> rolesRaw)) {
            return List.of();
        }
        List<String> roles = new ArrayList<>();
        for (Object role : rolesRaw) {
            if (role instanceof String roleName && !roleName.isBlank()) {
                roles.add(roleName);
            }
        }
        return roles;
    }

    /**
     * Извлекает роли клиентов из claim {@code resource_access.*.roles}.
     */
    private List<String> extractResourceRoles(Jwt jwt) {
        Object resourceAccessObj = jwt.getClaim("resource_access");
        if (!(resourceAccessObj instanceof Map<?, ?> resourceAccess)) {
            return List.of();
        }
        List<String> roles = new ArrayList<>();
        for (Object clientObj : resourceAccess.values()) {
            if (!(clientObj instanceof Map<?, ?> client)) {
                continue;
            }
            Object rolesObj = client.get("roles");
            if (!(rolesObj instanceof Collection<?> rolesRaw)) {
                continue;
            }
            for (Object role : rolesRaw) {
                if (role instanceof String roleName && !roleName.isBlank()) {
                    roles.add(roleName);
                }
            }
        }
        return roles;
    }
}
