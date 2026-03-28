package ru.practicum.cash.integration.notification.service;

import java.math.BigDecimal;

/**
 * Контракт отправки событий cash-сервиса в notification-сервис.
 */
public interface CashNotificationService {

    /**
     * Уведомляет о пополнении баланса пользователя.
     *
     * @param username username получателя
     * @param amount   сумма пополнения
     */
    void notifyCashDeposit(String username, BigDecimal amount);

    /**
     * Уведомляет о снятии средств с баланса пользователя.
     *
     * @param username username получателя
     * @param amount   сумма списания
     */
    void notifyCashWithdraw(String username, BigDecimal amount);
}
