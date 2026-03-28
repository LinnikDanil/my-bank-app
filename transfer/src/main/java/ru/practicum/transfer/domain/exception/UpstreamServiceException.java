package ru.practicum.transfer.domain.exception;

public class UpstreamServiceException extends TransferException {
    public UpstreamServiceException(String message) {
        super(message);
    }
}
