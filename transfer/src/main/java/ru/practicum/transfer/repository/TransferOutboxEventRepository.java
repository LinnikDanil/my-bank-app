package ru.practicum.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.transfer.domain.model.TransferOutboxEventEntity;
import ru.practicum.transfer.domain.model.TransferOutboxEventStatus;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TransferOutboxEventRepository extends JpaRepository<TransferOutboxEventEntity, UUID> {

    List<TransferOutboxEventEntity> findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            Collection<TransferOutboxEventStatus> statuses,
            OffsetDateTime dueAt
    );
}
