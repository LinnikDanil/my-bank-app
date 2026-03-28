package ru.practicum.transfer.domain.exception;

public abstract class TransferException extends RuntimeException {
    protected TransferException(String message) {
        super(message);
    }
}
