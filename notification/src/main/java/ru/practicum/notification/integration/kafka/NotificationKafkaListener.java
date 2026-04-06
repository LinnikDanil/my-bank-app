package ru.practicum.notification.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.practicum.common.tracing.TraceContextSupport;
import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.notification.service.NotificationService;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${integration.notification.topic}")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            populateTraceContext(record);
            NotificationEvent event = objectMapper.readValue(record.value(), NotificationEvent.class);
            notificationService.processEvent(event);
            acknowledgment.acknowledge();
        } catch (JsonProcessingException ex) {
            log.error("Некорректная структура сообщения {}", record.key(), ex);
            throw new IllegalArgumentException("Некорректная структура сообщения " + record.key(), ex);
        } catch (RuntimeException ex) {
            log.error("Внутренняя ошибка во время обработки записи {}", record.key(), ex);
            throw ex;
        } finally {
            TraceContextSupport.clearMdc();
        }
    }

    private void populateTraceContext(ConsumerRecord<String, String> record) {
        String traceId = headerValue(record, TraceContextSupport.TRACE_ID);
        String spanId = headerValue(record, TraceContextSupport.SPAN_ID);
        if (hasText(traceId) && hasText(spanId)) {
            TraceContextSupport.populateMdc(traceId, spanId);
            return;
        }

        TraceContextSupport.populateMdcFromTraceparent(headerValue(record, TraceContextSupport.TRACEPARENT));
    }

    private String headerValue(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        if (header == null || header.value() == null || header.value().length == 0) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

}
