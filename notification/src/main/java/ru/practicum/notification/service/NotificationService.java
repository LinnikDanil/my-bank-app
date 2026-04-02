package ru.practicum.notification.service;

import ru.practicum.common.notification.NotificationEvent;

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
