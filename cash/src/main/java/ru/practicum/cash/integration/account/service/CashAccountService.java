package ru.practicum.cash.integration.account.service;

import ru.practicum.cash.integration.account.domain.BalanceResponse;

import java.math.BigDecimal;

public interface CashAccountService {

    BalanceResponse deposit(String username, BigDecimal amount);

    BalanceResponse withdraw(String username, BigDecimal amount);
}
