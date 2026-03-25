package ru.practicum.account.domain.exception;

public class InsufficientFundsException extends AccountException {
    public InsufficientFundsException(String username) {
        super("Insufficient funds for username=" + username);
    }
}
