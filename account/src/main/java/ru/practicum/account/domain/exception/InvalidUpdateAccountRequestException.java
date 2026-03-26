package ru.practicum.account.domain.exception;

public class InvalidUpdateAccountRequestException extends AccountException {
    public InvalidUpdateAccountRequestException() {
        super("Update account request is invalid");
    }

    public InvalidUpdateAccountRequestException(String message) {
        super(message);
    }
}
