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
        void logsAccountUpdated() {
            notificationService.processEvent(TestDataFactory.accountUpdatedEvent());
        }

        @Test
        @DisplayName("logs cash deposit")
        void logsCashDeposit() {
            notificationService.processEvent(TestDataFactory.cashDepositEvent());
        }

        @Test
        @DisplayName("logs transfer completed")
        void logsTransferCompleted() {
            notificationService.processEvent(TestDataFactory.transferCompletedEvent());
        }
    }
}
