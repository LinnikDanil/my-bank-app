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
@Table(name = "transfer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferEntity {

    @Id
    private UUID id;

    @Column(name = "username_from", nullable = false, length = 64)
    private String usernameFrom;

    @Column(name = "username_to", nullable = false, length = 64)
    private String usernameTo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransferStatus status;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    public static TransferEntity newPending(String usernameFrom, String usernameTo, BigDecimal amount) {
        return TransferEntity.builder()
                .id(UUID.randomUUID())
                .usernameFrom(usernameFrom)
                .usernameTo(usernameTo)
                .amount(amount)
                .status(TransferStatus.PENDING)
                .build();
    }

    public void markCompleted() {
        this.status = TransferStatus.COMPLETED;
        this.failureReason = null;
    }

    public void markCompensating(String reason) {
        this.status = TransferStatus.COMPENSATING;
        this.failureReason = reason;
    }

    public void markFailed(String reason) {
        this.status = TransferStatus.FAILED;
        this.failureReason = reason;
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
