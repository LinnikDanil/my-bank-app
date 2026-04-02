package ru.practicum.notification.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.practicum.notification.util.TestDataFactory;

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
    }
}
