package ru.practicum.cash.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AuthenticationEntryPoint authenticationEntryPoint,
                                            AccessDeniedHandler accessDeniedHandler) throws Exception {
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

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        return (request, response, authException) -> resolver.resolveException(request, response, null, authException);
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        return (request, response, accessDeniedException) ->
                resolver.resolveException(request, response, null, accessDeniedException);
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

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
