package ru.practicum.cash.service;

import ru.practicum.cash.domain.CashOperationRequest;
import ru.practicum.cash.domain.CashOperationResponse;

/**
 * Бизнес-операции пополнения и снятия наличных для текущего пользователя.
 */
public interface CashService {

    /**
     * Пополняет баланс пользователя.
     *
     * @param username username пользователя-инициатора
     * @param request  сумма и параметры операции
     * @return результат операции с обновленным балансом
     */
    CashOperationResponse deposit(String username, CashOperationRequest request);

    /**
     * Списывает средства с баланса пользователя.
     *
     * @param username username пользователя-инициатора
     * @param request  сумма и параметры операции
     * @return результат операции с обновленным балансом
     */
    CashOperationResponse withdraw(String username, CashOperationRequest request);
}
