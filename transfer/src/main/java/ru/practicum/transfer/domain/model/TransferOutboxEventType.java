package ru.practicum.transfer.domain.model;

public enum TransferOutboxEventType {
    NOTIFY_TRANSFER_COMPLETED,
    COMPENSATE_TRANSFER
}
