package ru.practicum.transfer.domain.model;

public enum TransferOutboxEventStatus {
    NEW,
    PROCESSING,
    RETRY,
    PROCESSED,
    DEAD
}
