package ru.practicum.account.integration.notification.service;

import ru.practicum.account.domain.model.Account;

import java.math.BigDecimal;

public interface AccountNotificationService {
    void notifyAccountUpdated(Account account);

    void notifyCashDeposit(String username, BigDecimal amount);

    void notifyCashWithdraw(String username, BigDecimal amount);
}
