package ru.practicum.account.domain.exception;

public class InvalidUsernameException extends AccountException {
    public InvalidUsernameException() {
        super("Username is required");
    }
}
