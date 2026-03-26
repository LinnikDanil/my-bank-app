package ru.practicum.account.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityConfig")
class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Nested
    @DisplayName("jwtAuthenticationConverter")
    class JwtAuthenticationConverter {

        @Test
        @DisplayName("extracts authorities from realm and resource claims")
        void test1() {
            var jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("realm_access", Map.of("roles", List.of("USER", "ADMIN")))
                    .claim("resource_access", Map.of(
                            "account-service", Map.of("roles", List.of("CASH")),
                            "transfer-service", Map.of("roles", List.of("TRANSFER"))
                    ))
                    .build();

            var authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);
            var authorities = authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .toList();

            assertThat(authorities).contains("USER", "ADMIN", "CASH", "TRANSFER");
        }

        @Test
        @DisplayName("ignores blank roles and removes duplicates")
        void test2() {
            var jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("realm_access", Map.of("roles", List.of("USER", "USER", "   ")))
                    .claim("resource_access", Map.of(
                            "account-service", Map.of("roles", List.of("USER", "CASH")),
                            "broken-service", Map.of("roles", List.of("", "   "))
                    ))
                    .build();

            var authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);
            var authorities = authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .toList();

            assertThat(authorities).contains("USER", "CASH");
            assertThat(authorities.stream().filter("USER"::equals).count()).isEqualTo(1);
        }

        @Test
        @DisplayName("handles missing claims")
        void test3() {
            var jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("sub", "service-account")
                    .build();

            var authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);

            var authorities = authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .toList();
            assertThat(authorities)
                    .doesNotContain("USER", "ADMIN", "CASH", "TRANSFER");
        }
    }
}
