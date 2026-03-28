package ru.practicum.transfer.integration.account.service;

import ru.practicum.transfer.integration.account.domain.BalanceResponse;

import java.math.BigDecimal;

/**
 * Клиентский контракт взаимодействия transfer-сервиса с account-сервисом.
 */
public interface TransferAccountService {

    /**
     * Пополняет баланс пользователя в account-сервисе.
     *
     * @param username username получателя
     * @param amount   сумма пополнения
     * @return актуальный баланс пользователя
     */
    BalanceResponse deposit(String username, BigDecimal amount);

    /**
     * Списывает средства с баланса пользователя в account-сервисе.
     *
     * @param username username пользователя
     * @param amount   сумма списания
     * @return актуальный баланс пользователя
     */
    BalanceResponse withdraw(String username, BigDecimal amount);
}
