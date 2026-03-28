package ru.practicum.notification.util;

import ru.practicum.notification.domain.NotificationEvent;
import ru.practicum.notification.domain.NotificationEventPayload;
import ru.practicum.notification.domain.NotificationEventType;

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
        return new NotificationEvent(
                UUID.randomUUID(),
                NotificationEventType.ACCOUNT_UPDATED,
                OffsetDateTime.now(ZoneOffset.UTC),
                List.of("ivanivanov"),
                new NotificationEventPayload()
                        .username("ivanivanov")
                        .fullName("Ivan Ivanov")
                        .dateOfBirth(LocalDate.of(2001, 5, 10))
        );
    }

    public static NotificationEvent cashDepositEvent() {
        return new NotificationEvent(
                UUID.randomUUID(),
                NotificationEventType.CASH_DEPOSIT,
                OffsetDateTime.now(ZoneOffset.UTC),
                List.of("ivanivanov"),
                new NotificationEventPayload()
                        .username("ivanivanov")
                        .amount(new BigDecimal("100.00"))
        );
    }

    public static NotificationEvent cashWithdrawEvent() {
        return new NotificationEvent(
                UUID.randomUUID(),
                NotificationEventType.CASH_WITHDRAW,
                OffsetDateTime.now(ZoneOffset.UTC),
                List.of("ivanivanov"),
                new NotificationEventPayload()
                        .username("ivanivanov")
                        .amount(new BigDecimal("50.00"))
        );
    }

    public static NotificationEvent transferCompletedEvent() {
        return new NotificationEvent(
                UUID.randomUUID(),
                NotificationEventType.TRANSFER_COMPLETED,
                OffsetDateTime.now(ZoneOffset.UTC),
                List.of("ivanivanov", "petrpetrov"),
                new NotificationEventPayload()
                        .usernameFrom("ivanivanov")
                        .usernameTo("petrpetrov")
                        .amount(new BigDecimal("250.00"))
        );
    }
}
