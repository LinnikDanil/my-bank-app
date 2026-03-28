package ru.practicum.cash.domain.exception;

public class InvalidAmountException extends CashException {
    public InvalidAmountException() {
        super("Amount must be greater than zero");
    }
}
