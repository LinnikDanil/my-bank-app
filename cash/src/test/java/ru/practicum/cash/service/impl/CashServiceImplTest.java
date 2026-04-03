package ru.practicum.cash.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.cash.domain.exception.InvalidAmountException;
import ru.practicum.cash.domain.exception.InvalidCashOperationRequestException;
import ru.practicum.cash.domain.exception.InvalidUsernameException;
import ru.practicum.cash.integration.account.service.CashAccountService;
import ru.practicum.cash.integration.notification.service.CashNotificationService;
import ru.practicum.cash.util.TestDataFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashServiceImpl")
class CashServiceImplTest {

    private static final String USERNAME = TestDataFactory.USERNAME;

    @Mock
    private CashAccountService cashAccountService;

    @Mock
    private CashNotificationService cashNotificationService;

    @InjectMocks
    private CashServiceImpl cashService;

    @Nested
    @DisplayName("deposit")
    class Deposit {

        @Test
        @DisplayName("ok")
        void deposit_returnsUpdatedBalance_whenRequestIsValid() {
            var request = TestDataFactory.createRequest("250.50");
            var balance = TestDataFactory.createBalanceResponse("1250.50");

            when(cashAccountService.deposit(USERNAME, new BigDecimal("250.50"))).thenReturn(balance);

            var result = cashService.deposit(USERNAME, request);

            assertThat(result.getUsername()).isEqualTo(USERNAME);
            assertThat(result.getAmount()).isEqualByComparingTo("250.50");
            assertThat(result.getBalance()).isEqualByComparingTo("1250.50");
            verify(cashNotificationService, times(1)).notifyCashDeposit(USERNAME, new BigDecimal("250.50"));
        }

        @Test
        @DisplayName("invalid username")
        void deposit_throwsInvalidUsernameException_whenUsernameIsBlank() {
            assertThatExceptionOfType(InvalidUsernameException.class)
                    .isThrownBy(() -> cashService.deposit(" ", TestDataFactory.createRequest("10.00")));

            verifyNoInteractions(cashAccountService, cashNotificationService);
        }

        @Test
        @DisplayName("request is null")
        void deposit_throwsInvalidCashOperationRequestException_whenRequestIsNull() {
            assertThatExceptionOfType(InvalidCashOperationRequestException.class)
                    .isThrownBy(() -> cashService.deposit(USERNAME, null));

            verifyNoInteractions(cashAccountService, cashNotificationService);
        }

        @Test
        @DisplayName("amount must be positive")
        void deposit_throwsInvalidAmountException_whenAmountIsNotPositive() {
            assertThatExceptionOfType(InvalidAmountException.class)
                    .isThrownBy(() -> cashService.deposit(USERNAME, TestDataFactory.createRequest("0.00")));

            verifyNoInteractions(cashAccountService, cashNotificationService);
        }
    }

    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("ok")
        void withdraw_returnsUpdatedBalance_whenRequestIsValid() {
            var request = TestDataFactory.createRequest("125.00");
            var balance = TestDataFactory.createBalanceResponse("875.00");

            when(cashAccountService.withdraw(USERNAME, new BigDecimal("125.00"))).thenReturn(balance);

            var result = cashService.withdraw(USERNAME, request);

            assertThat(result.getUsername()).isEqualTo(USERNAME);
            assertThat(result.getAmount()).isEqualByComparingTo("125.00");
            assertThat(result.getBalance()).isEqualByComparingTo("875.00");
            verify(cashNotificationService, times(1)).notifyCashWithdraw(USERNAME, new BigDecimal("125.00"));
        }

        @Test
        @DisplayName("request is null")
        void withdraw_throwsInvalidCashOperationRequestException_whenRequestIsNull() {
            assertThatExceptionOfType(InvalidCashOperationRequestException.class)
                    .isThrownBy(() -> cashService.withdraw(USERNAME, null));

            verifyNoInteractions(cashAccountService, cashNotificationService);
        }

        @Test
        @DisplayName("amount must be positive")
        void withdraw_throwsInvalidAmountException_whenAmountIsNotPositive() {
            assertThatExceptionOfType(InvalidAmountException.class)
                    .isThrownBy(() -> cashService.withdraw(USERNAME, TestDataFactory.createRequest("-1.00")));

            verifyNoInteractions(cashAccountService, cashNotificationService);
        }
    }
}
