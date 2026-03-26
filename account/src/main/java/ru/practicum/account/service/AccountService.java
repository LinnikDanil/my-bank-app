package ru.practicum.account.service;

import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.internalapi.BalanceResponse;
import ru.practicum.account.domain.internalapi.MoneyAmountRequest;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;
import ru.practicum.account.domain.publicapi.UpdateAccountRequest;

/**
 * Контракт бизнес-логики сервиса аккаунтов.
 */
public interface AccountService {
    /**
     * Получение данных аккаунта текущего пользователя.
     *
     * @param username username пользователя из JWT
     */
    AccountResponse getCurrentAccount(String username);

    /**
     * Получение страницы получателей для перевода (без текущего пользователя).
     *
     * @param username username пользователя из JWT
     * @param page     номер страницы
     * @param size     размер страницы
     * @param search   строка фильтра username/fullName
     */
    RecipientPageResponse getRecipients(String username, Integer page, Integer size, String search);

    /**
     * Обновление профиля текущего пользователя.
     *
     * @param username             username пользователя из JWT
     * @param updateAccountRequest входные данные для обновления
     */
    AccountResponse updateCurrentAccount(String username, UpdateAccountRequest updateAccountRequest);

    /**
     * Пополнение баланса указанного пользователя.
     */
    BalanceResponse deposit(String username, MoneyAmountRequest moneyAmountRequest);

    /**
     * Списание средств с баланса указанного пользователя.
     */
    BalanceResponse withdraw(String username, MoneyAmountRequest moneyAmountRequest);
}
