package ru.practicum.cash.integration.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.cash.integration.notification.service.CashNotificationService;
import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.common.notification.NotificationEventPayload;
import ru.practicum.common.notification.NotificationEventType;
import ru.practicum.common.tracing.TraceContextSupport;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashNotificationServiceImpl implements CashNotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${integration.notification.topic}")
    private String notificationTopic;

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyCashDepositFallback")
    @Retry(name = "notificationService", fallbackMethod = "notifyCashDepositFallback")
    public void notifyCashDeposit(String username, BigDecimal amount) {
        NotificationEventPayload payload = NotificationEventPayload.builder()
                .username(username)
                .amount(amount)
                .build();

        sendEvent(List.of(username), NotificationEventType.CASH_DEPOSIT, payload);
    }

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyCashWithdrawFallback")
    @Retry(name = "notificationService", fallbackMethod = "notifyCashWithdrawFallback")
    public void notifyCashWithdraw(String username, BigDecimal amount) {
        NotificationEventPayload payload = NotificationEventPayload.builder()
                .username(username)
                .amount(amount)
                .build();

        sendEvent(List.of(username), NotificationEventType.CASH_WITHDRAW, payload);
    }

    private void sendEvent(List<String> recipients, NotificationEventType eventType, NotificationEventPayload payload) {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(recipients)
                .payload(payload)
                .build();

        try {
            String eventJson = objectMapper.writeValueAsString(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    notificationTopic,
                    event.getEventId().toString(),
                    eventJson
            );
            addTraceHeaders(record);
            Map<String, String> mdcContext = TraceContextSupport.snapshotMdc();

            kafkaTemplate.send(record)
                    .whenComplete((result, ex) -> {
                        TraceContextSupport.withMdc(mdcContext, () -> {
                            if (ex != null) {
                                log.error("Не удалось отправить событие в Kafka: eventId={}, type={}",
                                        event.getEventId(), eventType, ex);
                                return;
                            }
                            log.info("Событие отправлено в Kafka: eventId={}, type={}, partition={}, offset={}",
                                    event.getEventId(),
                                    eventType,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        });
                    });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать notification-событие", e);
        }
    }

    private void addTraceHeaders(ProducerRecord<String, String> record) {
        TraceContextSupport.TraceHeaders traceHeaders = TraceContextSupport.currentTraceHeaders();
        if (!traceHeaders.isPresent()) {
            return;
        }

        record.headers().add(
                TraceContextSupport.TRACE_ID,
                traceHeaders.traceId().getBytes(StandardCharsets.UTF_8)
        );
        record.headers().add(
                TraceContextSupport.SPAN_ID,
                traceHeaders.spanId().getBytes(StandardCharsets.UTF_8)
        );
        record.headers().add(
                TraceContextSupport.TRACEPARENT,
                traceHeaders.traceparent().getBytes(StandardCharsets.UTF_8)
        );
    }

    private void notifyCashDepositFallback(String username, BigDecimal amount, Throwable throwable) {
        log.error("Не удалось отправить событие CASH_DEPOSIT для пользователя {} на сумму {}", username, amount, throwable);
    }

    private void notifyCashWithdrawFallback(String username, BigDecimal amount, Throwable throwable) {
        log.error("Не удалось отправить событие CASH_WITHDRAW для пользователя {} на сумму {}", username, amount, throwable);
    }
}
