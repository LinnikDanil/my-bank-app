package ru.practicum.transfer.domain.exception;

public class AccountNotFoundException extends TransferException {
    public AccountNotFoundException(String username) {
        super("Account not found for username=" + username);
    }
}
