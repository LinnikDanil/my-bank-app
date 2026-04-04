package ru.practicum.notification.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.notification.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${integration.notification.topic}")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            NotificationEvent event = objectMapper.readValue(record.value(), NotificationEvent.class);
            notificationService.processEvent(event);
            acknowledgment.acknowledge();
        } catch (JsonProcessingException ex) {
            log.error("Некорректная структура сообщения {}", record.key(), ex);
            throw new IllegalArgumentException("Некорректная структура сообщения " + record.key(), ex);
        } catch (RuntimeException ex) {
            log.error("Внутренняя ошибка во время обработки записи {}", record.key(), ex);
            throw ex;
        }
    }

}
