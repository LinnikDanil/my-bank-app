package ru.practicum.transfer.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "transfer_outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferOutboxEventEntity {

    @Id
    private UUID id;

    @Column(name = "transfer_id", nullable = false)
    private UUID transferId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private TransferOutboxEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransferOutboxEventStatus status;

    @Column(name = "username_from", nullable = false, length = 64)
    private String usernameFrom;

    @Column(name = "username_to", nullable = false, length = 64)
    private String usernameTo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at", nullable = false)
    private OffsetDateTime nextAttemptAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public static TransferOutboxEventEntity compensationEvent(TransferEntity transfer) {
        return TransferOutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .transferId(transfer.getId())
                .eventType(TransferOutboxEventType.COMPENSATE_TRANSFER)
                .status(TransferOutboxEventStatus.NEW)
                .usernameFrom(transfer.getUsernameFrom())
                .usernameTo(transfer.getUsernameTo())
                .amount(transfer.getAmount())
                .attemptCount(0)
                .nextAttemptAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    public static TransferOutboxEventEntity transferCompletedNotificationEvent(TransferEntity transfer) {
        return TransferOutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .transferId(transfer.getId())
                .eventType(TransferOutboxEventType.NOTIFY_TRANSFER_COMPLETED)
                .status(TransferOutboxEventStatus.NEW)
                .usernameFrom(transfer.getUsernameFrom())
                .usernameTo(transfer.getUsernameTo())
                .amount(transfer.getAmount())
                .attemptCount(0)
                .nextAttemptAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    public void markProcessing() {
        this.status = TransferOutboxEventStatus.PROCESSING;
    }

    public void markProcessed() {
        this.status = TransferOutboxEventStatus.PROCESSED;
        this.lastError = null;
    }

    public void markRetry(OffsetDateTime nextAttemptAt, String errorMessage) {
        this.status = TransferOutboxEventStatus.RETRY;
        this.attemptCount += 1;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = errorMessage;
    }

    public void markDead(String errorMessage) {
        this.status = TransferOutboxEventStatus.DEAD;
        this.attemptCount += 1;
        this.lastError = errorMessage;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
