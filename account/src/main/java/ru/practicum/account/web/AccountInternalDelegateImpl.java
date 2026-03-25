package ru.practicum.account.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.account.api.internalapi.AccountInternalApiDelegate;
import ru.practicum.account.domain.internalapi.BalanceResponse;
import ru.practicum.account.domain.internalapi.MoneyAmountRequest;
import ru.practicum.account.service.AccountService;

@Component
@RequiredArgsConstructor
public class AccountInternalDelegateImpl implements AccountInternalApiDelegate {

    private final AccountService accountService;

    @Override
    public ResponseEntity<BalanceResponse> internalDeposit(String username, MoneyAmountRequest moneyAmountRequest) {
        BalanceResponse response = accountService.deposit(username, moneyAmountRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BalanceResponse> internalWithdraw(String username, MoneyAmountRequest moneyAmountRequest) {
        BalanceResponse response = accountService.withdraw(username, moneyAmountRequest);
        return ResponseEntity.ok(response);
    }
}
