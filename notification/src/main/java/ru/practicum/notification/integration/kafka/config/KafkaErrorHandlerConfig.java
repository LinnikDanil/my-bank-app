package ru.practicum.notification.integration.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonDelegatingErrorHandler;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import ru.practicum.notification.integration.kafka.DeadLetterQueueRecordRecoverer;

import java.util.Map;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    CommonErrorHandler commonErrorHandler(DeadLetterQueueRecordRecoverer deadLetterQueueRecordRecoverer) {
        DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(
                deadLetterQueueRecordRecoverer,
                new FixedBackOff(1000L, 9L)
        );

        DefaultErrorHandler deadLetterQueueErrorHandler = new DefaultErrorHandler(
                deadLetterQueueRecordRecoverer,
                new FixedBackOff(0L, 0L)
        );

        CommonDelegatingErrorHandler errorHandler = new CommonDelegatingErrorHandler(defaultErrorHandler);
        errorHandler.setErrorHandlers(Map.of(IllegalArgumentException.class, deadLetterQueueErrorHandler));

        return errorHandler;
    }
}
