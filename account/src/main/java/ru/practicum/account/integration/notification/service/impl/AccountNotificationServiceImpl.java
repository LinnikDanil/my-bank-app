package ru.practicum.account.integration.notification.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AccountNotificationServiceImpl implements AccountNotificationService {

    private final NotificationInternalApi notificationInternalApi;

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyAccountUpdatedFallback")
    @Retry(name = "notificationService", fallbackMethod = "notifyAccountUpdatedFallback")
    public void notifyAccountUpdated(Account account) {
        NotificationEventPayload payload = new NotificationEventPayload()
                .username(account.getUsername())
                .fullName(account.getFullName())
                .dateOfBirth(account.getDateOfBirth());

        sendEvent(account.getUsername(), NotificationEventType.ACCOUNT_UPDATED, payload);
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyCashDepositFallback")
    @Retry(name = "notificationService", fallbackMethod = "notifyCashDepositFallback")
    public void notifyCashDeposit(String username, BigDecimal amount) {
        NotificationEventPayload payload = new NotificationEventPayload()
                .username(username)
                .amount(amount);

        sendEvent(username, NotificationEventType.CASH_DEPOSIT, payload);
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyCashWithdrawFallback")
    @Retry(name = "notificationService", fallbackMethod = "notifyCashWithdrawFallback")
    public void notifyCashWithdraw(String username, BigDecimal amount) {
        NotificationEventPayload payload = new NotificationEventPayload()
                .username(username)
                .amount(amount);

        sendEvent(username, NotificationEventType.CASH_WITHDRAW, payload);
    }

    private void sendEvent(String recipient, NotificationEventType eventType, NotificationEventPayload payload) {
        log.info("Отправка события в notification-service: type={}, recipient={}", eventType, recipient);
        NotificationEvent event = new NotificationEvent()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(List.of(recipient))
                .payload(payload);
        notificationInternalApi.sendNotificationEvent(event);
        log.info("Событие отправлено в notification-service: eventId={}, type={}", event.getEventId(), eventType);
    }

    private void notifyAccountUpdatedFallback(Account account, Throwable throwable) {
        log.error("Не удалось отправить событие ACCOUNT_UPDATED для пользователя {}", account.getUsername(), throwable);
    }

    private void notifyCashDepositFallback(String username, BigDecimal amount, Throwable throwable) {
        log.error("Не удалось отправить событие CASH_DEPOSIT для пользователя {} на сумму {}", username, amount, throwable);
    }

    private void notifyCashWithdrawFallback(String username, BigDecimal amount, Throwable throwable) {
        log.error("Не удалось отправить событие CASH_WITHDRAW для пользователя {} на сумму {}", username, amount, throwable);
    }
}
