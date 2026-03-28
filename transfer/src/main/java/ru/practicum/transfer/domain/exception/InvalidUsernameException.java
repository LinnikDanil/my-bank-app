package ru.practicum.transfer.domain.exception;

public class InvalidUsernameException extends TransferException {
    public InvalidUsernameException() {
        super("Username is required");
    }
}
