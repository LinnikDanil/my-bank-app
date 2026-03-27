package ru.practicum.transfer.integration.account.service;

import ru.practicum.transfer.integration.account.domain.BalanceResponse;

import java.math.BigDecimal;

public interface TransferAccountService {

    BalanceResponse deposit(String username, BigDecimal amount);

    BalanceResponse withdraw(String username, BigDecimal amount);
}
