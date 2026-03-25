package ru.practicum.account.domain.exception;

public class InvalidPaginationException extends AccountException {
    public InvalidPaginationException() {
        super("Invalid pagination params: page must be >= 0 and size must be > 0");
    }
}
