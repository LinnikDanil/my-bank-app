package ru.practicum.cash.domain.exception;

public abstract class CashException extends RuntimeException {
    protected CashException(String message) {
        super(message);
    }
}
