package ru.practicum.transfer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.practicum.transfer.domain.model.TransferOutboxEventEntity;
import ru.practicum.transfer.domain.model.TransferOutboxEventStatus;
import ru.practicum.transfer.repository.TransferOutboxEventRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransferOutboxProcessor {

    private final TransferOutboxEventRepository transferOutboxEventRepository;
    private final TransferOutboxWorker transferOutboxWorker;

    @Scheduled(fixedDelayString = "${transfer.outbox.poll-delay-ms}")
    public void processDueEvents() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<TransferOutboxEventEntity> dueEvents =
                transferOutboxEventRepository.findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                        List.of(TransferOutboxEventStatus.NEW, TransferOutboxEventStatus.RETRY),
                        now
                );

        dueEvents.forEach(event -> transferOutboxWorker.processSingleEvent(event.getId()));
    }
}
