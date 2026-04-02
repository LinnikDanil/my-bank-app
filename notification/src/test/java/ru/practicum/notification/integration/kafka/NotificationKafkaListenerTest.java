package ru.practicum.notification.integration.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.common.notification.NotificationEventPayload;
import ru.practicum.common.notification.NotificationEventType;
import ru.practicum.notification.service.NotificationService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationKafkaListener unit")
class NotificationKafkaListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Acknowledgment acknowledgment;

    private final ObjectMapper objectMapper = com.fasterxml.jackson.databind.json.JsonMapper.builder().findAndAddModules().build();

    @Test
    @DisplayName("acks message when deserialization and processing succeeded")
    void test1() {
        NotificationKafkaListener listener = new NotificationKafkaListener(notificationService, objectMapper);
        var record = new org.apache.kafka.clients.consumer.ConsumerRecord<>(
                "notification-events", 0, 0L, "k1", buildEventJson()
        );

        listener.onMessage(record, acknowledgment);

        verify(notificationService).processEvent(any(NotificationEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("throws IllegalArgumentException on malformed json")
    void test2() {
        NotificationKafkaListener listener = new NotificationKafkaListener(notificationService, objectMapper);
        var record = new org.apache.kafka.clients.consumer.ConsumerRecord<>(
                "notification-events", 0, 0L, "k1", "{invalid"
        );

        assertThatThrownBy(() -> listener.onMessage(record, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Некорректная структура сообщения");
    }

    @Test
    @DisplayName("rethrows runtime exception from service")
    void test3() {
        NotificationKafkaListener listener = new NotificationKafkaListener(notificationService, objectMapper);
        var record = new org.apache.kafka.clients.consumer.ConsumerRecord<>(
                "notification-events", 0, 0L, "k1", buildEventJson()
        );
        doThrow(new RuntimeException("boom")).when(notificationService).processEvent(any(NotificationEvent.class));

        assertThatThrownBy(() -> listener.onMessage(record, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("boom");
    }

    private String buildEventJson() {
        try {
            return objectMapper.writeValueAsString(
                    NotificationEvent.builder()
                            .eventId(UUID.randomUUID())
                            .eventType(NotificationEventType.CASH_DEPOSIT)
                            .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                            .recipients(List.of("ivanivanov"))
                            .payload(NotificationEventPayload.builder()
                                    .username("ivanivanov")
                                    .amount(java.math.BigDecimal.TEN)
                                    .build())
                            .build()
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
