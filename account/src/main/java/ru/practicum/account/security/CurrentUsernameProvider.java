package ru.practicum.account.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import ru.practicum.account.domain.exception.InvalidUsernameException;

@Component
public class CurrentUsernameProvider {

    public String requireUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new InvalidUsernameException();
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new InvalidUsernameException();
        }

        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        String username = jwt.getClaimAsString("username");
        if (username != null && !username.isBlank()) {
            return username;
        }

        throw new InvalidUsernameException();
    }
}
