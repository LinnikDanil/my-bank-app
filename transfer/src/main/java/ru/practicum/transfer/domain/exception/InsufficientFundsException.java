package ru.practicum.transfer.domain.exception;

public class InsufficientFundsException extends TransferException {
    public InsufficientFundsException(String username) {
        super("Insufficient funds for username=" + username);
    }
}
