package ru.practicum.cash.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.practicum.cash.domain.exception.InvalidUsernameException;

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
        void test1() {
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
        @DisplayName("throws when principal is not jwt")
        void test2() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("user", "password")
            );

            assertThatExceptionOfType(InvalidUsernameException.class)
                    .isThrownBy(currentUsernameProvider::requireUsername);
        }
    }
}
