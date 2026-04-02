package ru.practicum.notification.dlq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.notification.dlq.domain.DeadLetterQueueEntity;

public interface DeadLetterQueueRepository extends JpaRepository<DeadLetterQueueEntity, Long> {
}
