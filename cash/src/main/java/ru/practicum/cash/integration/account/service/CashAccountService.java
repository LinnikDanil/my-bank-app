package ru.practicum.cash.integration.account.service;

import ru.practicum.cash.integration.account.domain.BalanceResponse;

import java.math.BigDecimal;

/**
 * Клиентский контракт взаимодействия cash-сервиса с account-сервисом.
 */
public interface CashAccountService {

    /**
     * Пополняет баланс пользователя через account-сервис.
     *
     * @param username username пользователя
     * @param amount   сумма пополнения
     * @return актуальный баланс пользователя
     */
    BalanceResponse deposit(String username, BigDecimal amount);

    /**
     * Списывает средства с баланса пользователя через account-сервис.
     *
     * @param username username пользователя
     * @param amount   сумма списания
     * @return актуальный баланс пользователя
     */
    BalanceResponse withdraw(String username, BigDecimal amount);
}
