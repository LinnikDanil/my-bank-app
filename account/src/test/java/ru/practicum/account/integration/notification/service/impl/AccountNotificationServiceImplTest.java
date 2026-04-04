package ru.practicum.account.integration.notification.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.account.domain.model.Account;
import ru.practicum.common.notification.NotificationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountNotificationServiceImpl unit")
class AccountNotificationServiceImplTest {

    private static final String TOPIC = "notification-events";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;
    private AccountNotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        objectMapper = com.fasterxml.jackson.databind.json.JsonMapper.builder().findAndAddModules().build();
        service = new AccountNotificationServiceImpl(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(service, "notificationTopic", TOPIC);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("notifyAccountUpdated sends ACCOUNT_UPDATED event")
    void testNotifyAccountUpdated() throws Exception {
        Account account = Account.builder()
                .username("ivanivanov")
                .fullName("Ivan Ivanov")
                .dateOfBirth(LocalDate.of(2001, 5, 10))
                .balance(new BigDecimal("100.00"))
                .build();

        service.notifyAccountUpdated(account);

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq(TOPIC), anyString(), valueCaptor.capture());

        NotificationEvent event = objectMapper.readValue(valueCaptor.getValue(), NotificationEvent.class);
        assertThat(event.getEventType().name()).isEqualTo("ACCOUNT_UPDATED");
        assertThat(event.getPayload().getUsername()).isEqualTo("ivanivanov");
        assertThat(event.getPayload().getFullName()).isEqualTo("Ivan Ivanov");
    }

    @Test
    @DisplayName("notifyCashDeposit sends CASH_DEPOSIT event")
    void testNotifyCashDeposit() throws Exception {
        service.notifyCashDeposit("ivanivanov", new BigDecimal("10.50"));

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq(TOPIC), anyString(), valueCaptor.capture());

        NotificationEvent event = objectMapper.readValue(valueCaptor.getValue(), NotificationEvent.class);
        assertThat(event.getEventType().name()).isEqualTo("CASH_DEPOSIT");
        assertThat(event.getPayload().getAmount()).isEqualByComparingTo("10.50");
    }

    @Test
    @DisplayName("notifyCashWithdraw sends CASH_WITHDRAW event")
    void testNotifyCashWithdraw() throws Exception {
        service.notifyCashWithdraw("ivanivanov", new BigDecimal("7.25"));

        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq(TOPIC), anyString(), valueCaptor.capture());

        NotificationEvent event = objectMapper.readValue(valueCaptor.getValue(), NotificationEvent.class);
        assertThat(event.getEventType().name()).isEqualTo("CASH_WITHDRAW");
        assertThat(event.getPayload().getAmount()).isEqualByComparingTo("7.25");
    }
}
