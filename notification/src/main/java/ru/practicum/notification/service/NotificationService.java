package ru.practicum.notification.service;

import ru.practicum.notification.domain.NotificationEvent;

/**
 * Контракт обработки событий уведомлений.
 */
public interface NotificationService {

    /**
     * Обрабатывает входящее событие уведомления.
     *
     * @param event событие с получателями и полезной нагрузкой
     */
    void processEvent(NotificationEvent event);
}
