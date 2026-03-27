package ru.practicum.cash.domain.exception;

public class InsufficientFundsException extends CashException {
    public InsufficientFundsException(String username) {
        super("Insufficient funds for username=" + username);
    }
}
