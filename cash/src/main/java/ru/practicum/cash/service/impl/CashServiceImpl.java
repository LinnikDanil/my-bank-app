package ru.practicum.cash.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.cash.domain.CashOperationRequest;
import ru.practicum.cash.domain.CashOperationResponse;
import ru.practicum.cash.domain.exception.InvalidAmountException;
import ru.practicum.cash.domain.exception.InvalidCashOperationRequestException;
import ru.practicum.cash.domain.exception.InvalidUsernameException;
import ru.practicum.cash.integration.account.service.CashAccountService;
import ru.practicum.cash.integration.notification.service.CashNotificationService;
import ru.practicum.cash.service.CashService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {

    private final CashAccountService cashAccountService;
    private final CashNotificationService cashNotificationService;

    @Override
    public CashOperationResponse deposit(String username, CashOperationRequest request) {
        validateUsername(username);
        validateRequest(request);

        BigDecimal amount = request.getAmount();
        log.info("Пополнение баланса: username={}, amount={}", username, amount);
        var balanceResponse = cashAccountService.deposit(username, amount);

        cashNotificationService.notifyCashDeposit(username, amount);
        log.info("Пополнение выполнено: username={}, newBalance={}", username, balanceResponse.getBalance());
        return new CashOperationResponse(username, amount, balanceResponse.getBalance());
    }

    @Override
    public CashOperationResponse withdraw(String username, CashOperationRequest request) {
        validateUsername(username);
        validateRequest(request);

        BigDecimal amount = request.getAmount();
        log.info("Снятие средств: username={}, amount={}", username, amount);
        var balanceResponse = cashAccountService.withdraw(username, amount);

        cashNotificationService.notifyCashWithdraw(username, amount);
        log.info("Снятие выполнено: username={}, newBalance={}", username, balanceResponse.getBalance());
        return new CashOperationResponse(username, amount, balanceResponse.getBalance());
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUsernameException();
        }
    }

    private void validateRequest(CashOperationRequest request) {
        if (request == null) {
            throw new InvalidCashOperationRequestException();
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
    }
}
