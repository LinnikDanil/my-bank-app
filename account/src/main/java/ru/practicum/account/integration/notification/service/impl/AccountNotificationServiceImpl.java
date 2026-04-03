package ru.practicum.account.integration.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.account.domain.model.Account;
import ru.practicum.account.integration.notification.service.AccountNotificationService;
import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.common.notification.NotificationEventPayload;
import ru.practicum.common.notification.NotificationEventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountNotificationServiceImpl implements AccountNotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${integration.notification.topic}")
    private String notificationTopic;

    @Override
    @CircuitBreaker(name = "notificationService", fallbackMethod = "notifyAccountUpdatedFallback")
    @Retry(name = "notificationService", fallbackMethod = "notifyAccountUpdatedFallback")
    public void notifyAccountUpdated(Account account) {
        NotificationEventPayload payload = NotificationEventPayload.builder()
                .username(account.getUsername())
                .fullName(account.getFullName())
                .dateOfBirth(account.getDateOfBirth())
                .build();

        sendEvent(List.of(account.getUsername()), NotificationEventType.ACCOUNT_UPDATED, payload);
    }

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
            kafkaTemplate.send(notificationTopic, event.getEventId().toString(), eventJson)
                    .whenComplete((result, ex) -> {
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
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать notification-событие", e);
        }
    }

    private void notifyAccountUpdatedFallback(Account account, Throwable throwable) {
        log.error("Не удалось отправить событие ACCOUNT_UPDATED для пользователя {}", account.getUsername(), throwable);
    }

    private void notifyCashDepositFallback(String username, BigDecimal amount, Throwable throwable) {
        log.error("Не удалось отправить событие CASH_DEPOSIT для пользователя {} на сумму {}", username, amount, throwable);
    }

    private void notifyCashWithdrawFallback(String username, BigDecimal amount, Throwable throwable) {
        log.error("Не удалось отправить событие CASH_WITHDRAW для пользователя {} на сумму {}", username, amount, throwable);
    }
}
