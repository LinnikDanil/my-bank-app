package ru.practicum.account.integration.notification.service;

import ru.practicum.account.domain.model.Account;

import java.math.BigDecimal;

/**
 * Контракт отправки доменных событий аккаунта в notification-сервис.
 */
public interface AccountNotificationService {

    /**
     * Отправляет событие об обновлении профиля пользователя.
     *
     * @param account актуальное состояние аккаунта
     */
    void notifyAccountUpdated(Account account);

    /**
     * Отправляет событие о пополнении баланса пользователя.
     *
     * @param username username получателя
     * @param amount   сумма операции
     */
    void notifyCashDeposit(String username, BigDecimal amount);

    /**
     * Отправляет событие о снятии средств с баланса пользователя.
     *
     * @param username username получателя
     * @param amount   сумма операции
     */
    void notifyCashWithdraw(String username, BigDecimal amount);
}
