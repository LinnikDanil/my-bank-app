package ru.practicum.transfer.domain.exception;

public class InvalidTransferRequestException extends TransferException {
    public InvalidTransferRequestException() {
        super("Transfer request is invalid");
    }

    public InvalidTransferRequestException(String message) {
        super(message);
    }
}
