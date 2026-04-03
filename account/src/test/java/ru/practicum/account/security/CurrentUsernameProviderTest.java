package ru.practicum.account.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.practicum.account.domain.exception.InvalidUsernameException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("CurrentUsernameProvider")
class CurrentUsernameProviderTest {

    private final CurrentUsernameProvider currentUsernameProvider = new CurrentUsernameProvider();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("requireUsername")
    class RequireUsername {

        @Test
        @DisplayName("returns preferred_username")
        void returnsPreferredUsername() {
            var jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("preferred_username", "ivanivanov")
                    .claim("username", "fallback")
                    .build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

            var result = currentUsernameProvider.requireUsername();

            assertThat(result).isEqualTo("ivanivanov");
        }

        @Test
        @DisplayName("returns username fallback")
        void returnsUsernameFallback() {
            var jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("preferred_username", "   ")
                    .claim("username", "petrpetrov")
                    .build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

            var result = currentUsernameProvider.requireUsername();

            assertThat(result).isEqualTo("petrpetrov");
        }

        @Test
        @DisplayName("throws when authentication is missing")
        void throwsWhenAuthenticationIsMissing() {
            assertThatExceptionOfType(InvalidUsernameException.class)
                    .isThrownBy(currentUsernameProvider::requireUsername);
        }

        @Test
        @DisplayName("throws when principal is not jwt")
        void throwsWhenPrincipalIsNotJwt() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("user", "password")
            );

            assertThatExceptionOfType(InvalidUsernameException.class)
                    .isThrownBy(currentUsernameProvider::requireUsername);
        }

        @Test
        @DisplayName("throws when claims are blank")
        void throwsWhenClaimsAreBlank() {
            var jwt = Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("preferred_username", "   ")
                    .claim("username", "")
                    .build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

            assertThatExceptionOfType(InvalidUsernameException.class)
                    .isThrownBy(currentUsernameProvider::requireUsername);
        }
    }
}
