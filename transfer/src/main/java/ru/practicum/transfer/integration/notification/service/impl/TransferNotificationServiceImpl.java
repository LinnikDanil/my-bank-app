package ru.practicum.transfer.integration.notification.service.impl;

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
import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.common.notification.NotificationEventPayload;
import ru.practicum.common.notification.NotificationEventType;
import ru.practicum.common.tracing.TraceContextSupport;
import ru.practicum.transfer.domain.exception.UpstreamServiceException;
import ru.practicum.transfer.integration.notification.service.TransferNotificationService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferNotificationServiceImpl implements TransferNotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${integration.notification.topic}")
    private String notificationTopic;

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyTransferCompletedFallback")
    @Retry(name = "notificationService", fallbackMethod = "notifyTransferCompletedFallback")
    public void notifyTransferCompleted(String usernameFrom, String usernameTo, BigDecimal amount) {
        NotificationEventPayload payload = NotificationEventPayload.builder()
                .usernameFrom(usernameFrom)
                .usernameTo(usernameTo)
                .amount(amount)
                .build();

        sendEvent(List.of(usernameFrom, usernameTo), NotificationEventType.TRANSFER_COMPLETED, payload);
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
            var sendResult = kafkaTemplate.send(record).get();
            if (sendResult != null && sendResult.getRecordMetadata() != null) {
                log.info("Событие отправлено в Kafka: eventId={}, type={}, partition={}, offset={}",
                        event.getEventId(),
                        eventType,
                        sendResult.getRecordMetadata().partition(),
                        sendResult.getRecordMetadata().offset());
            } else {
                log.info("Событие отправлено в Kafka: eventId={}, type={}", event.getEventId(), eventType);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать notification-событие", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UpstreamServiceException("Отправка notification-события была прервана");
        } catch (ExecutionException e) {
            throw new UpstreamServiceException("Не удалось отправить notification-событие в Kafka");
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

    private void notifyTransferCompletedFallback(String usernameFrom,
                                                 String usernameTo,
                                                 BigDecimal amount,
                                                 Throwable throwable) {
        log.error("Не удалось отправить событие TRANSFER_COMPLETED: {} -> {}, сумма {}",
                usernameFrom, usernameTo, amount, throwable);
    }
}
