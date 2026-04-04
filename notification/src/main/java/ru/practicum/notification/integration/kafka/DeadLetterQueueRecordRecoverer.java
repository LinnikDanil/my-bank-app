package ru.practicum.notification.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.stereotype.Service;
import ru.practicum.notification.dlq.domain.DeadLetterQueueEntity;
import ru.practicum.notification.dlq.repository.DeadLetterQueueRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeadLetterQueueRecordRecoverer implements ConsumerRecordRecoverer {

    private final DeadLetterQueueRepository repository;

    @Override
    public void accept(ConsumerRecord<?, ?> record, Exception e) {
        DeadLetterQueueEntity entity = new DeadLetterQueueEntity();
        entity.setMsgTopic(record.topic());
        entity.setMsgPartition(record.partition());
        entity.setMsgOffset(record.offset());
        if (record.key() != null) {
            entity.setMsgKey(record.key().toString());
        }
        if (record.value() != null) {
            entity.setMsgValue(record.value().toString());
        }
        entity.setErrorMessage(e.getMessage());
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        repository.save(entity);
        log.info("Сообщение {} сохранено в DLQ", record.key());
    }
}
