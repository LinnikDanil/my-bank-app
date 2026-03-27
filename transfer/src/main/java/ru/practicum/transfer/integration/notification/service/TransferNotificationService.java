package ru.practicum.transfer.integration.notification.service;

import java.math.BigDecimal;

public interface TransferNotificationService {

    void notifyTransferCompleted(String usernameFrom, String usernameTo, BigDecimal amount);
}
