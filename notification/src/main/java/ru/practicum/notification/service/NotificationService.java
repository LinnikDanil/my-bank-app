package ru.practicum.notification.service;

import ru.practicum.notification.domain.NotificationEvent;

public interface NotificationService {

    void processEvent(NotificationEvent event);
}
