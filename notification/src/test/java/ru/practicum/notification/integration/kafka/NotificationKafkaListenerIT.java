package ru.practicum.notification.integration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.common.notification.NotificationEventPayload;
import ru.practicum.common.notification.NotificationEventType;
import ru.practicum.notification.service.NotificationService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        classes = NotificationKafkaListenerIT.TestConfig.class,
        properties = {
                "integration.notification.topic=notification-events",
                "spring.kafka.consumer.group-id=notification-it-group",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer"
        }
)
@EmbeddedKafka(topics = NotificationKafkaListenerIT.TOPIC, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@DisplayName("Notification Kafka consumer integration")
class NotificationKafkaListenerIT {

    static final String TOPIC = "notification-events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationService notificationService;

    @Test
    @DisplayName("consumes message from Kafka and calls NotificationService")
    void test1() throws Exception {
        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(NotificationEventType.TRANSFER_COMPLETED)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(List.of("ivanivanov", "petrpetrov"))
                .payload(NotificationEventPayload.builder()
                        .usernameFrom("ivanivanov")
                        .usernameTo("petrpetrov")
                        .amount(new BigDecimal("100.00"))
                        .build())
                .build();

        kafkaTemplate.send(TOPIC, event.getEventId().toString(), objectMapper.writeValueAsString(event));

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationService, timeout(5000).times(1)).processEvent(captor.capture());
        assertThat(captor.getValue().getEventType()).isEqualTo(NotificationEventType.TRANSFER_COMPLETED);
    }

    @Configuration
    @EnableKafka
    @Import(ru.practicum.common.config.CommonJacksonAutoConfiguration.class)
    static class TestConfig {

        @Bean
        ProducerFactory<String, String> producerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        ConsumerFactory<String, String> consumerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                @Value("${spring.kafka.consumer.group-id}") String groupId,
                @Value("${spring.kafka.consumer.auto-offset-reset}") String autoOffsetReset) {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class);
            return new DefaultKafkaConsumerFactory<>(props);
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
                ConsumerFactory<String, String> consumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, String> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(
                    org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE);
            return factory;
        }

        @Bean
        NotificationService notificationService() {
            return mock(NotificationService.class);
        }

        @Bean
        NotificationKafkaListener notificationKafkaListener(NotificationService notificationService,
                                                            ObjectMapper objectMapper) {
            return new NotificationKafkaListener(notificationService, objectMapper);
        }
    }
}
