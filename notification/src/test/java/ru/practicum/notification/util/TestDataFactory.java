package ru.practicum.notification.util;

import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.common.notification.NotificationEventPayload;
import ru.practicum.common.notification.NotificationEventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static NotificationEvent accountUpdatedEvent() {
        return NotificationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(NotificationEventType.ACCOUNT_UPDATED)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(List.of("ivanivanov"))
                .payload(NotificationEventPayload.builder()
                        .username("ivanivanov")
                        .fullName("Ivan Ivanov")
                        .dateOfBirth(LocalDate.of(2001, 5, 10))
                        .build())
                .build();
    }

    public static NotificationEvent cashDepositEvent() {
        return NotificationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(NotificationEventType.CASH_DEPOSIT)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(List.of("ivanivanov"))
                .payload(NotificationEventPayload.builder()
                        .username("ivanivanov")
                        .amount(new BigDecimal("100.00"))
                        .build())
                .build();
    }

    public static NotificationEvent transferCompletedEvent() {
        return NotificationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(NotificationEventType.TRANSFER_COMPLETED)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(List.of("ivanivanov", "petrpetrov"))
                .payload(NotificationEventPayload.builder()
                        .usernameFrom("ivanivanov")
                        .usernameTo("petrpetrov")
                        .amount(new BigDecimal("250.00"))
                        .build())
                .build();
    }
}
