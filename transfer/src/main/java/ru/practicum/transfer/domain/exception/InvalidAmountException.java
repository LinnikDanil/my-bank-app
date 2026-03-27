package ru.practicum.transfer.domain.exception;

public class InvalidAmountException extends TransferException {
    public InvalidAmountException() {
        super("Amount must be greater than zero");
    }
}
