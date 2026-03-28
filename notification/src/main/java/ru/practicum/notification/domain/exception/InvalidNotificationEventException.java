package ru.practicum.notification.domain.exception;

public class InvalidNotificationEventException extends NotificationException {
    public InvalidNotificationEventException() {
        super("Notification event is invalid");
    }

    public InvalidNotificationEventException(String message) {
        super(message);
    }
}
