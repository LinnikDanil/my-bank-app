package ru.practicum.account.service;

import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.internalapi.BalanceResponse;
import ru.practicum.account.domain.internalapi.MoneyAmountRequest;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;
import ru.practicum.account.domain.publicapi.UpdateAccountRequest;

public interface AccountService {
    AccountResponse getCurrentAccount();

    RecipientPageResponse getRecipients(Integer page, Integer size, String search);

    AccountResponse updateCurrentAccount(UpdateAccountRequest updateAccountRequest);

    BalanceResponse deposit(String username, MoneyAmountRequest moneyAmountRequest);

    BalanceResponse withdraw(String username, MoneyAmountRequest moneyAmountRequest);
}
