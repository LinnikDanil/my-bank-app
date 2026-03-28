package ru.practicum.cash.domain.exception;

public class UpstreamServiceException extends CashException {
    public UpstreamServiceException(String message) {
        super(message);
    }
}
