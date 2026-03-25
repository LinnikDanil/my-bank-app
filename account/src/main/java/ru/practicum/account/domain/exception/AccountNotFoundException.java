package ru.practicum.account.domain.exception;

public class AccountNotFoundException extends AccountException {
    public AccountNotFoundException(String username) {
        super("Account not found for username=" + username);
    }
}
