package ru.practicum.notification.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.common.notification.NotificationEventPayload;
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
        String message = switch (event.getEventType()) {
            case ACCOUNT_UPDATED -> formatAccountUpdated(event);
            case CASH_DEPOSIT -> formatCashDeposit(event);
            case CASH_WITHDRAW -> formatCashWithdraw(event);
            case TRANSFER_COMPLETED -> formatTransferCompleted(event);
        };

        log.info(message);
    }

    private String formatAccountUpdated(NotificationEvent event) {
        NotificationEventPayload p = event.getPayload();

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
        NotificationEventPayload p = event.getPayload();

        return formatPrettyBlock(
                event,
                "CASH DEPOSIT",
                List.of(
                        "User: " + p.getUsername(),
                        "Amount: +" + amount(p.getAmount())
                )
        );
    }

    private String formatCashWithdraw(NotificationEvent event) {
        NotificationEventPayload p = event.getPayload();

        return formatPrettyBlock(
                event,
                "CASH WITHDRAW",
                List.of(
                        "User: " + p.getUsername(),
                        "Amount: -" + amount(p.getAmount())
                )
        );
    }

    private String formatTransferCompleted(NotificationEvent event) {
        NotificationEventPayload p = event.getPayload();

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
}
