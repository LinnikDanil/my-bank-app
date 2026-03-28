package ru.practicum.account.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import ru.practicum.account.api.internalapi.AccountInternalApiDelegate;
import ru.practicum.account.domain.internalapi.BalanceResponse;
import ru.practicum.account.domain.internalapi.MoneyAmountRequest;
import ru.practicum.account.service.AccountService;

/**
 * Реализация внутреннего API аккаунта для межсервисных операций баланса.
 */
@Component
@RequiredArgsConstructor
public class AccountInternalDelegateImpl implements AccountInternalApiDelegate {

    private final AccountService accountService;

    /**
     * Пополняет баланс пользователя.
     * Вызывается сервисами, имеющими authority CASH или TRANSFER.
     */
    @Override
    @PreAuthorize("hasAnyAuthority('CASH','TRANSFER')")
    public ResponseEntity<BalanceResponse> internalDeposit(String username, MoneyAmountRequest moneyAmountRequest) {
        BalanceResponse response = accountService.deposit(username, moneyAmountRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Списывает средства с баланса пользователя.
     * Вызывается сервисами, имеющими authority CASH или TRANSFER.
     */
    @Override
    @PreAuthorize("hasAnyAuthority('CASH','TRANSFER')")
    public ResponseEntity<BalanceResponse> internalWithdraw(String username, MoneyAmountRequest moneyAmountRequest) {
        BalanceResponse response = accountService.withdraw(username, moneyAmountRequest);
        return ResponseEntity.ok(response);
    }
}
