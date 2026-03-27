package ru.practicum.cash.integration.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.cash.integration.notification.api.NotificationInternalApi;
import ru.practicum.cash.integration.notification.domain.NotificationEvent;
import ru.practicum.cash.integration.notification.domain.NotificationEventPayload;
import ru.practicum.cash.integration.notification.domain.NotificationEventType;
import ru.practicum.cash.integration.notification.service.CashNotificationService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashNotificationServiceImpl implements CashNotificationService {

    private final NotificationInternalApi notificationInternalApi;

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
        log.info("Отправка события в notification-service: type={}, recipient={}", eventType, recipient);
        NotificationEvent event = new NotificationEvent()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(List.of(recipient))
                .payload(payload);

        try {
            notificationInternalApi.sendNotificationEvent(event);
            log.info("Событие отправлено в notification-service: eventId={}, type={}", event.getEventId(), eventType);
        } catch (Exception ex) {
            log.error("Не удалось отправить событие в notification-service: eventId={}, type={}",
                    event.getEventId(), eventType, ex);
        }
    }
}
