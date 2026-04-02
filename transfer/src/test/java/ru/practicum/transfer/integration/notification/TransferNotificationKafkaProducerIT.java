package ru.practicum.transfer.integration.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import ru.practicum.common.config.CommonJacksonAutoConfiguration;
import ru.practicum.common.notification.NotificationEvent;
import ru.practicum.transfer.integration.notification.service.impl.TransferNotificationServiceImpl;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = TransferNotificationKafkaProducerIT.TestConfig.class,
        properties = {
                "integration.notification.topic=notification-events"
        }
)
@EmbeddedKafka(topics = TransferNotificationKafkaProducerIT.TOPIC, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@DisplayName("Transfer Kafka producer integration")
class TransferNotificationKafkaProducerIT {

    static final String TOPIC = "notification-events";

    @Autowired
    private TransferNotificationServiceImpl producer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("sends TRANSFER_COMPLETED event to Kafka")
    void test1() throws Exception {
        try (var consumerForTest = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps(embeddedKafkaBroker, "transfer-it", true),
                new StringDeserializer(),
                new StringDeserializer()
        ).createConsumer()) {
            consumerForTest.subscribe(List.of(TOPIC));

            producer.notifyTransferCompleted("ivanivanov", "petrpetrov", new BigDecimal("75.00"));

            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumerForTest, TOPIC, Duration.ofSeconds(5));
            NotificationEvent event = objectMapper.readValue(record.value(), NotificationEvent.class);

            assertThat(event.getEventType().name()).isEqualTo("TRANSFER_COMPLETED");
            assertThat(event.getPayload().getUsernameFrom()).isEqualTo("ivanivanov");
            assertThat(event.getPayload().getUsernameTo()).isEqualTo("petrpetrov");
            assertThat(event.getPayload().getAmount()).isEqualByComparingTo("75.00");
        }
    }

    @Configuration
    @Import(CommonJacksonAutoConfiguration.class)
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
        TransferNotificationServiceImpl transferNotificationService(KafkaTemplate<String, String> kafkaTemplate,
                                                                    ObjectMapper objectMapper) {
            return new TransferNotificationServiceImpl(kafkaTemplate, objectMapper);
        }
    }
}
