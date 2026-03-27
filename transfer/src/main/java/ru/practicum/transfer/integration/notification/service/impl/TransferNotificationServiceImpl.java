package ru.practicum.transfer.integration.notification.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.transfer.integration.notification.api.NotificationInternalApi;
import ru.practicum.transfer.integration.notification.domain.NotificationEvent;
import ru.practicum.transfer.integration.notification.domain.NotificationEventPayload;
import ru.practicum.transfer.integration.notification.domain.NotificationEventType;
import ru.practicum.transfer.integration.notification.service.TransferNotificationService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferNotificationServiceImpl implements TransferNotificationService {

    private final NotificationInternalApi notificationInternalApi;

    @Override
    public void notifyTransferCompleted(String usernameFrom, String usernameTo, BigDecimal amount) {
        NotificationEventPayload payload = new NotificationEventPayload()
                .usernameFrom(usernameFrom)
                .usernameTo(usernameTo)
                .amount(amount);

        sendEvent(List.of(usernameFrom, usernameTo), NotificationEventType.TRANSFER_COMPLETED, payload);
    }

    private void sendEvent(List<String> recipients, NotificationEventType eventType, NotificationEventPayload payload) {
        log.info("Отправка события в notification-service: type={}, recipients={}", eventType, recipients);
        NotificationEvent event = new NotificationEvent()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(recipients)
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
