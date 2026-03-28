package ru.practicum.cash.domain.exception;

public class AccountNotFoundException extends CashException {
    public AccountNotFoundException(String username) {
        super("Account not found for username=" + username);
    }
}
