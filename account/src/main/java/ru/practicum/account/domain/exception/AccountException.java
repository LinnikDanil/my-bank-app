package ru.practicum.account.domain.exception;

public abstract class AccountException extends RuntimeException {
    protected AccountException(String message) {
        super(message);
    }
}
