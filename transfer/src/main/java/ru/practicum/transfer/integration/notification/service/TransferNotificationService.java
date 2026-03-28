package ru.practicum.transfer.integration.notification.service;

import java.math.BigDecimal;

/**
 * Контракт отправки уведомлений transfer-сервиса в notification-сервис.
 */
public interface TransferNotificationService {

    /**
     * Отправляет событие об успешном переводе между пользователями.
     *
     * @param usernameFrom username отправителя
     * @param usernameTo   username получателя
     * @param amount       сумма перевода
     */
    void notifyTransferCompleted(String usernameFrom, String usernameTo, BigDecimal amount);
}
