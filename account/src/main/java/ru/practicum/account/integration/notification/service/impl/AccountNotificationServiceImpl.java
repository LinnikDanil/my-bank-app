package ru.practicum.account.integration.notification.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.account.domain.model.Account;
import ru.practicum.account.integration.notification.api.NotificationInternalApi;
import ru.practicum.account.integration.notification.domain.NotificationEvent;
import ru.practicum.account.integration.notification.domain.NotificationEventPayload;
import ru.practicum.account.integration.notification.domain.NotificationEventType;
import ru.practicum.account.integration.notification.service.AccountNotificationService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountNotificationServiceImpl implements AccountNotificationService {

    private final NotificationInternalApi notificationInternalApi;

    @Override
    public void notifyAccountUpdated(Account account) {
        NotificationEventPayload payload = new NotificationEventPayload()
                .username(account.getUsername())
                .fullName(account.getFullName())
                .dateOfBirth(account.getDateOfBirth());

        sendEvent(account.getUsername(), NotificationEventType.ACCOUNT_UPDATED, payload);
    }

    @Override
    public void notifyCashDeposit(String username, BigDecimal amount) {
        NotificationEventPayload payload = new NotificationEventPayload()
                .username(username)
                .amount(amount);

        sendEvent(username, NotificationEventType.CASH_DEPOSIT, payload);
    }

    @Override
    public void notifyCashWithdraw(String username, BigDecimal amount) {
        NotificationEventPayload payload = new NotificationEventPayload()
                .username(username)
                .amount(amount);

        sendEvent(username, NotificationEventType.CASH_WITHDRAW, payload);
    }

    private void sendEvent(String recipient, NotificationEventType eventType, NotificationEventPayload payload) {
        NotificationEvent event = new NotificationEvent()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(List.of(recipient))
                .payload(payload);

        notificationInternalApi.sendNotificationEvent(event);
    }
}
