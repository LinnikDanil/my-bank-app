package ru.practicum.account.service;

import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.internalapi.BalanceResponse;
import ru.practicum.account.domain.internalapi.MoneyAmountRequest;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;
import ru.practicum.account.domain.publicapi.UpdateAccountRequest;

public interface AccountService {
    AccountResponse getCurrentAccount(String username);

    RecipientPageResponse getRecipients(String username, Integer page, Integer size, String search);

    AccountResponse updateCurrentAccount(String username, UpdateAccountRequest updateAccountRequest);

    BalanceResponse deposit(String username, MoneyAmountRequest moneyAmountRequest);

    BalanceResponse withdraw(String username, MoneyAmountRequest moneyAmountRequest);
}
