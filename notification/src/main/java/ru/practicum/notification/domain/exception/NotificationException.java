package ru.practicum.notification.domain.exception;

public abstract class NotificationException extends RuntimeException {
    protected NotificationException(String message) {
        super(message);
    }
}
