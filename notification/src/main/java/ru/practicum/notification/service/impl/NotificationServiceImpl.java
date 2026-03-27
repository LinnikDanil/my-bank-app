package ru.practicum.notification.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.notification.domain.NotificationEvent;
import ru.practicum.notification.domain.NotificationEventPayload;
import ru.practicum.notification.domain.NotificationEventType;
import ru.practicum.notification.domain.exception.InvalidNotificationEventException;
import ru.practicum.notification.service.NotificationService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");

    @Override
    public void processEvent(NotificationEvent event) {
        validateEvent(event);

        String message = switch (event.getEventType()) {
            case ACCOUNT_UPDATED -> formatAccountUpdated(event);
            case CASH_DEPOSIT -> formatCashDeposit(event);
            case CASH_WITHDRAW -> formatCashWithdraw(event);
            case TRANSFER_COMPLETED -> formatTransferCompleted(event);
        };

        log.info(message);
    }

    private void validateEvent(NotificationEvent event) {
        if (event == null) {
            throw new InvalidNotificationEventException();
        }
        if (event.getEventId() == null || event.getEventType() == null || event.getTimestamp() == null) {
            throw new InvalidNotificationEventException("eventId, eventType and timestamp are required");
        }
        if (event.getRecipients() == null || event.getRecipients().isEmpty()) {
            throw new InvalidNotificationEventException("recipients must not be empty");
        }
        boolean hasBlankRecipient = event.getRecipients().stream().anyMatch(r -> r == null || r.isBlank());
        if (hasBlankRecipient) {
            throw new InvalidNotificationEventException("recipients contain blank value");
        }
        if (event.getPayload() == null) {
            throw new InvalidNotificationEventException("payload is required");
        }
    }

    private String formatAccountUpdated(NotificationEvent event) {
        NotificationEventPayload p = event.getPayload();
        requireNotBlank(p.getUsername(), "payload.username is required");
        requireNotBlank(p.getFullName(), "payload.fullName is required");
        if (p.getDateOfBirth() == null) {
            throw new InvalidNotificationEventException("payload.dateOfBirth is required");
        }

        return formatPrettyBlock(
                event,
                "PROFILE UPDATED",
                List.of(
                        "User: " + p.getUsername(),
                        "Full name: " + p.getFullName(),
                        "Date of birth: " + p.getDateOfBirth()
                )
        );
    }

    private String formatCashDeposit(NotificationEvent event) {
        CashPayloadData data = extractCashPayloadData(event.getPayload());

        return formatPrettyBlock(
                event,
                "CASH DEPOSIT",
                List.of(
                        "User: " + data.username(),
                        "Amount: +" + amount(data.amount())
                )
        );
    }

    private String formatCashWithdraw(NotificationEvent event) {
        CashPayloadData data = extractCashPayloadData(event.getPayload());

        return formatPrettyBlock(
                event,
                "CASH WITHDRAW",
                List.of(
                        "User: " + data.username(),
                        "Amount: -" + amount(data.amount())
                )
        );
    }

    private String formatTransferCompleted(NotificationEvent event) {
        NotificationEventPayload p = event.getPayload();
        requireNotBlank(p.getUsernameFrom(), "payload.usernameFrom is required");
        requireNotBlank(p.getUsernameTo(), "payload.usernameTo is required");
        if (p.getAmount() == null) {
            throw new InvalidNotificationEventException("payload.amount is required");
        }

        return formatPrettyBlock(
                event,
                "TRANSFER COMPLETED",
                List.of(
                        "From: " + p.getUsernameFrom(),
                        "To: " + p.getUsernameTo(),
                        "Amount: " + amount(p.getAmount())
                )
        );
    }

    private CashPayloadData extractCashPayloadData(NotificationEventPayload payload) {
        requireNotBlank(payload.getUsername(), "payload.username is required");
        if (payload.getAmount() == null) {
            throw new InvalidNotificationEventException("payload.amount is required");
        }
        return new CashPayloadData(payload.getUsername(), payload.getAmount());
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidNotificationEventException(message);
        }
    }

    private String formatPrettyBlock(NotificationEvent event, String title, List<String> lines) {
        String recipients = event.getRecipients().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));

        return String.format(
                """
                        ==========================================
                        [NOTIFICATION] %s
                        Event ID: %s
                        Timestamp: %s
                        Recipients: %s
                        ------------------------------------------
                        %s
                        ==========================================
                        """,
                title,
                event.getEventId(),
                formatTimestamp(event.getTimestamp()),
                recipients,
                String.join(System.lineSeparator(), lines)
        );
    }

    private String formatTimestamp(OffsetDateTime timestamp) {
        return timestamp.withOffsetSameInstant(ZoneOffset.UTC).format(TS_FORMATTER);
    }

    private String amount(BigDecimal amount) {
        return amount == null ? "n/a" : amount.stripTrailingZeros().toPlainString();
    }

    private record CashPayloadData(String username, BigDecimal amount) {
    }
}
