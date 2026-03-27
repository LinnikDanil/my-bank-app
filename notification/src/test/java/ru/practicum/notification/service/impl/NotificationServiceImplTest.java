package ru.practicum.notification.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.notification.domain.NotificationEvent;
import ru.practicum.notification.domain.NotificationEventPayload;
import ru.practicum.notification.domain.NotificationEventType;
import ru.practicum.notification.domain.exception.InvalidNotificationEventException;
import ru.practicum.notification.util.TestDataFactory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("NotificationServiceImpl")
class NotificationServiceImplTest {

    private final NotificationServiceImpl notificationService = new NotificationServiceImpl();

    @Nested
    @DisplayName("processEvent")
    class ProcessEvent {

        @Test
        @DisplayName("logs account updated")
        void test1() {
            notificationService.processEvent(TestDataFactory.accountUpdatedEvent());
        }

        @Test
        @DisplayName("logs cash deposit")
        void test2() {
            notificationService.processEvent(TestDataFactory.cashDepositEvent());
        }

        @Test
        @DisplayName("logs transfer completed")
        void test3() {
            notificationService.processEvent(TestDataFactory.transferCompletedEvent());
        }

        @Test
        @DisplayName("throws on null event")
        void test4() {
            assertThatExceptionOfType(InvalidNotificationEventException.class)
                    .isThrownBy(() -> notificationService.processEvent(null));
        }

        @Test
        @DisplayName("throws on payload mismatch")
        void test5() {
            NotificationEvent mismatch = new NotificationEvent(
                    UUID.randomUUID(),
                    NotificationEventType.CASH_DEPOSIT,
                    OffsetDateTime.now(ZoneOffset.UTC),
                    List.of("ivanivanov"),
                    new NotificationEventPayload()
                            .usernameFrom("ivanivanov")
                            .usernameTo("petrpetrov")
                            .amount(new BigDecimal("1.00"))
            );

            assertThatExceptionOfType(InvalidNotificationEventException.class)
                    .isThrownBy(() -> notificationService.processEvent(mismatch));
        }
    }
}
