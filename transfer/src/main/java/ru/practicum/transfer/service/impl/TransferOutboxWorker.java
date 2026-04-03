package ru.practicum.transfer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.transfer.domain.model.TransferEntity;
import ru.practicum.transfer.domain.model.TransferOutboxEventEntity;
import ru.practicum.transfer.domain.model.TransferOutboxEventStatus;
import ru.practicum.transfer.domain.model.TransferOutboxEventType;
import ru.practicum.transfer.integration.account.service.TransferAccountService;
import ru.practicum.transfer.integration.notification.service.TransferNotificationService;
import ru.practicum.transfer.repository.TransferOutboxEventRepository;
import ru.practicum.transfer.repository.TransferRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferOutboxWorker {

    private final TransferOutboxEventRepository transferOutboxEventRepository;
    private final TransferRepository transferRepository;
    private final TransferAccountService transferAccountService;
    private final TransferNotificationService transferNotificationService;

    @Value("${transfer.outbox.retry-delay-ms}")
    private long retryDelayMs;

    @Value("${transfer.outbox.max-attempts}")
    private int maxAttempts;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleEvent(UUID eventId) {
        TransferOutboxEventEntity event = transferOutboxEventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return;
        }
        if (event.getStatus() == TransferOutboxEventStatus.PROCESSED || event.getStatus() == TransferOutboxEventStatus.DEAD) {
            return;
        }
        if (event.getNextAttemptAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            return;
        }

        event.markProcessing();
        transferOutboxEventRepository.save(event);

        try {
            if (event.getEventType() == TransferOutboxEventType.COMPENSATE_TRANSFER) {
                processCompensation(event);
            } else if (event.getEventType() == TransferOutboxEventType.NOTIFY_TRANSFER_COMPLETED) {
                transferNotificationService.notifyTransferCompleted(
                        event.getUsernameFrom(),
                        event.getUsernameTo(),
                        event.getAmount()
                );
            }
            event.markProcessed();
            transferOutboxEventRepository.save(event);
        } catch (RuntimeException ex) {
            reschedule(event, ex);
        }
    }

    private void processCompensation(TransferOutboxEventEntity event) {
        transferAccountService.deposit(event.getUsernameFrom(), event.getAmount());
        transferRepository.findById(event.getTransferId())
                .ifPresent(transfer -> updateTransferAfterCompensation(transfer, event));
    }

    private void updateTransferAfterCompensation(TransferEntity transfer, TransferOutboxEventEntity event) {
        transfer.markFailed("Compensation completed by outbox event " + event.getId());
        transferRepository.save(transfer);
    }

    private void reschedule(TransferOutboxEventEntity event, Exception ex) {
        int nextAttempt = event.getAttemptCount() + 1;
        if (nextAttempt >= maxAttempts) {
            event.markDead(ex.getMessage());
            transferOutboxEventRepository.save(event);
            if (event.getEventType() == TransferOutboxEventType.COMPENSATE_TRANSFER) {
                transferRepository.findById(event.getTransferId())
                        .ifPresent(transfer -> markTransferFailedAfterDeadCompensation(transfer, event, ex, nextAttempt));
            }
            log.error("Событие outbox переведено в DEAD: eventId={}, type={}, attempts={}",
                    event.getId(), event.getEventType(), nextAttempt, ex);
            return;
        }

        long effectiveDelayMs = retryDelayMs * nextAttempt;
        OffsetDateTime nextAttemptAt = OffsetDateTime.now(ZoneOffset.UTC).plusNanos(effectiveDelayMs * 1_000_000);
        event.markRetry(nextAttemptAt, ex.getMessage());
        transferOutboxEventRepository.save(event);
        log.warn("Ошибка обработки outbox-события, запланирован повтор: eventId={}, type={}, nextAttemptAt={}",
                event.getId(), event.getEventType(), nextAttemptAt, ex);
    }

    private void markTransferFailedAfterDeadCompensation(TransferEntity transfer,
                                                         TransferOutboxEventEntity event,
                                                         Exception ex,
                                                         int attempts) {
        transfer.markFailed("Compensation failed after " + attempts + " attempts. outboxEventId="
                + event.getId() + ", reason=" + ex.getMessage());
        transferRepository.save(transfer);
    }
}
