package ru.practicum.cash.domain.exception;

public class InvalidCashOperationRequestException extends CashException {
    public InvalidCashOperationRequestException() {
        super("Cash operation request is required");
    }
}
