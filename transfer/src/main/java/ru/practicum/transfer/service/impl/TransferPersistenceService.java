package ru.practicum.transfer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.transfer.domain.exception.UpstreamServiceException;
import ru.practicum.transfer.domain.model.TransferEntity;
import ru.practicum.transfer.domain.model.TransferOutboxEventEntity;
import ru.practicum.transfer.repository.TransferOutboxEventRepository;
import ru.practicum.transfer.repository.TransferRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferPersistenceService {

    private final TransferRepository transferRepository;
    private final TransferOutboxEventRepository transferOutboxEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransferEntity createPendingTransfer(String usernameFrom, String usernameTo, BigDecimal amount) {
        return transferRepository.save(TransferEntity.newPending(usernameFrom, usernameTo, amount));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueueCompensation(UUID transferId, String failureReason) {
        TransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new UpstreamServiceException("Transfer not found while scheduling compensation"));

        transfer.markCompensating(failureReason);
        transferRepository.save(transfer);
        transferOutboxEventRepository.save(TransferOutboxEventEntity.compensationEvent(transfer));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompletedAndEnqueueNotification(UUID transferId) {
        TransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new UpstreamServiceException("Transfer not found while completing transfer"));

        transfer.markCompleted();
        transferRepository.save(transfer);
        transferOutboxEventRepository.save(TransferOutboxEventEntity.transferCompletedNotificationEvent(transfer));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID transferId, String failureReason) {
        TransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new UpstreamServiceException("Transfer not found while marking transfer failed"));

        transfer.markFailed(failureReason);
        transferRepository.save(transfer);
    }
}
