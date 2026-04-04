package ru.practicum.transfer.integration.notification.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.common.notification.NotificationEvent;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferNotificationServiceImpl unit")
class TransferNotificationServiceImplTest {

    private static final String TOPIC = "notification-events";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;
    private TransferNotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = com.fasterxml.jackson.databind.json.JsonMapper.builder().findAndAddModules().build();
        service = new TransferNotificationServiceImpl(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(service, "notificationTopic", TOPIC);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("notifyTransferCompleted sends TRANSFER_COMPLETED event")
    void testNotifyTransferCompleted() throws Exception {
        service.notifyTransferCompleted("ivanivanov", "petrpetrov", new BigDecimal("333.33"));

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq(TOPIC), anyString(), valueCaptor.capture());

        NotificationEvent event = objectMapper.readValue(valueCaptor.getValue(), NotificationEvent.class);
        assertThat(event.getEventType().name()).isEqualTo("TRANSFER_COMPLETED");
        assertThat(event.getPayload().getUsernameFrom()).isEqualTo("ivanivanov");
        assertThat(event.getPayload().getUsernameTo()).isEqualTo("petrpetrov");
        assertThat(event.getPayload().getAmount()).isEqualByComparingTo("333.33");
    }
}
