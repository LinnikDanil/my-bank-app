package ru.practicum.account.integration.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hamcrest.MatcherAssert;
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
import org.springframework.kafka.test.hamcrest.KafkaMatchers;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import ru.practicum.account.domain.model.Account;
import ru.practicum.account.integration.notification.service.impl.AccountNotificationServiceImpl;
import ru.practicum.common.config.CommonJacksonAutoConfiguration;
import ru.practicum.common.notification.NotificationEvent;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = AccountNotificationKafkaProducerIT.TestConfig.class,
        properties = {
                "integration.notification.topic=notification-events"
        }
)
@EmbeddedKafka(topics = AccountNotificationKafkaProducerIT.TOPIC)
@DisplayName("Account Kafka producer integration")
class AccountNotificationKafkaProducerIT {

    static final String TOPIC = "notification-events";

    @Autowired
    private AccountNotificationServiceImpl producer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("sends ACCOUNT_UPDATED event to Kafka")
    void test1() throws Exception {
        try (var consumerForTest = new DefaultKafkaConsumerFactory<>(
                KafkaTestUtils.consumerProps("account-it", "true", embeddedKafkaBroker),
                new StringDeserializer(),
                new StringDeserializer()
        ).createConsumer()) {
            consumerForTest.subscribe(List.of(TOPIC));

            Account account = Account.builder()
                    .username("ivanivanov")
                    .fullName("Ivan Ivanov")
                    .dateOfBirth(LocalDate.of(2001, 5, 10))
                    .balance(new BigDecimal("100.00"))
                    .build();

            producer.notifyAccountUpdated(account);

            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumerForTest, TOPIC, Duration.ofSeconds(5));
            MatcherAssert.assertThat(record, KafkaMatchers.hasValue(record.value()));

            NotificationEvent event = objectMapper.readValue(record.value(), NotificationEvent.class);
            assertThat(event.getEventType().name()).isEqualTo("ACCOUNT_UPDATED");
            assertThat(event.getPayload().getUsername()).isEqualTo("ivanivanov");
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
        AccountNotificationServiceImpl accountNotificationService(KafkaTemplate<String, String> kafkaTemplate,
                                                                  ObjectMapper objectMapper) {
            return new AccountNotificationServiceImpl(kafkaTemplate, objectMapper);
        }
    }
}
