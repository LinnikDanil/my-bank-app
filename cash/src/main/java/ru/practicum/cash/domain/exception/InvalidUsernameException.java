package ru.practicum.cash.domain.exception;

public class InvalidUsernameException extends CashException {
    public InvalidUsernameException() {
        super("Username is required");
    }
}
