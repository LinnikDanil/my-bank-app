package ru.practicum.cash.integration.notification.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.common.notification.NotificationEvent;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashNotificationServiceImpl unit")
class CashNotificationServiceImplTest {

    private static final String TOPIC = "notification-events";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;
    private CashNotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = com.fasterxml.jackson.databind.json.JsonMapper.builder().findAndAddModules().build();
        service = new CashNotificationServiceImpl(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(service, "notificationTopic", TOPIC);
        @SuppressWarnings("unchecked")
        SendResult<String, String> sendResult = mock(SendResult.class);
        RecordMetadata recordMetadata = mock(RecordMetadata.class);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(sendResult));
    }

    @Test
    @DisplayName("notifyCashDeposit sends CASH_DEPOSIT event")
    void testNotifyCashDeposit() throws Exception {
        service.notifyCashDeposit("ivanivanov", new BigDecimal("111.11"));

        ArgumentCaptor<ProducerRecord<String, String>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(recordCaptor.capture());

        NotificationEvent event = objectMapper.readValue(recordCaptor.getValue().value(), NotificationEvent.class);
        assertThat(event.getEventType().name()).isEqualTo("CASH_DEPOSIT");
        assertThat(event.getPayload().getUsername()).isEqualTo("ivanivanov");
        assertThat(event.getPayload().getAmount()).isEqualByComparingTo("111.11");
    }

    @Test
    @DisplayName("notifyCashWithdraw sends CASH_WITHDRAW event")
    void testNotifyCashWithdraw() throws Exception {
        service.notifyCashWithdraw("ivanivanov", new BigDecimal("22.22"));

        ArgumentCaptor<ProducerRecord<String, String>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(recordCaptor.capture());

        NotificationEvent event = objectMapper.readValue(recordCaptor.getValue().value(), NotificationEvent.class);
        assertThat(event.getEventType().name()).isEqualTo("CASH_WITHDRAW");
        assertThat(event.getPayload().getAmount()).isEqualByComparingTo("22.22");
    }
}
