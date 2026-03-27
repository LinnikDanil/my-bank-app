package ru.practicum.cash.integration.notification.service;

import java.math.BigDecimal;

public interface CashNotificationService {

    void notifyCashDeposit(String username, BigDecimal amount);

    void notifyCashWithdraw(String username, BigDecimal amount);
}
