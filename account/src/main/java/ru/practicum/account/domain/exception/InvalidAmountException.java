package ru.practicum.account.domain.exception;

public class InvalidAmountException extends AccountException {
    public InvalidAmountException() {
        super("Amount must be greater than zero");
    }
}
