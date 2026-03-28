package ru.practicum.transfer.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.transfer.domain.exception.InvalidAmountException;
import ru.practicum.transfer.domain.exception.InvalidTransferRequestException;
import ru.practicum.transfer.domain.exception.InvalidUsernameException;
import ru.practicum.transfer.domain.exception.UpstreamServiceException;
import ru.practicum.transfer.integration.account.service.TransferAccountService;
import ru.practicum.transfer.integration.notification.service.TransferNotificationService;
import ru.practicum.transfer.util.TestDataFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferServiceImpl")
class TransferServiceImplTest {

    private static final String USERNAME_FROM = TestDataFactory.USERNAME_FROM;
    private static final String USERNAME_TO = TestDataFactory.USERNAME_TO;

    @Mock
    private TransferAccountService transferAccountService;

    @Mock
    private TransferNotificationService transferNotificationService;

    @InjectMocks
    private TransferServiceImpl transferService;

    @Nested
    @DisplayName("transfer")
    class Transfer {

        @Test
        @DisplayName("ok")
        void test1() {
            var request = TestDataFactory.createRequest("250.00");

            when(transferAccountService.withdraw(USERNAME_FROM, new BigDecimal("250.00")))
                    .thenReturn(TestDataFactory.createBalanceResponse(USERNAME_FROM, "750.00"));
            when(transferAccountService.deposit(USERNAME_TO, new BigDecimal("250.00")))
                    .thenReturn(TestDataFactory.createBalanceResponse(USERNAME_TO, "1250.00"));

            var result = transferService.transfer(USERNAME_FROM, request);

            assertThat(result.getUsernameFrom()).isEqualTo(USERNAME_FROM);
            assertThat(result.getUsernameTo()).isEqualTo(USERNAME_TO);
            assertThat(result.getAmount()).isEqualByComparingTo("250.00");
            verify(transferNotificationService, times(1))
                    .notifyTransferCompleted(USERNAME_FROM, USERNAME_TO, new BigDecimal("250.00"));
        }

        @Test
        @DisplayName("invalid username")
        void test2() {
            assertThatExceptionOfType(InvalidUsernameException.class)
                    .isThrownBy(() -> transferService.transfer(" ", TestDataFactory.createRequest("10.00")));

            verifyNoInteractions(transferAccountService, transferNotificationService);
        }

        @Test
        @DisplayName("request is null")
        void test3() {
            assertThatExceptionOfType(InvalidTransferRequestException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, null));

            verifyNoInteractions(transferAccountService, transferNotificationService);
        }

        @Test
        @DisplayName("amount must be positive")
        void test4() {
            assertThatExceptionOfType(InvalidAmountException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, new ru.practicum.transfer.domain.TransferRequest(USERNAME_TO, BigDecimal.ZERO)));

            verifyNoInteractions(transferAccountService, transferNotificationService);
        }

        @Test
        @DisplayName("usernameTo must be different")
        void test5() {
            var request = new ru.practicum.transfer.domain.TransferRequest(USERNAME_FROM, new BigDecimal("10.00"));

            assertThatExceptionOfType(InvalidTransferRequestException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, request));

            verifyNoInteractions(transferAccountService, transferNotificationService);
        }

        @Test
        @DisplayName("compensates when deposit to recipient fails")
        void test6() {
            var request = TestDataFactory.createRequest("250.00");
            var depositFailure = new UpstreamServiceException("deposit failed");

            when(transferAccountService.withdraw(USERNAME_FROM, new BigDecimal("250.00")))
                    .thenReturn(TestDataFactory.createBalanceResponse(USERNAME_FROM, "750.00"));
            when(transferAccountService.deposit(USERNAME_TO, new BigDecimal("250.00")))
                    .thenThrow(depositFailure);
            when(transferAccountService.deposit(USERNAME_FROM, new BigDecimal("250.00")))
                    .thenReturn(TestDataFactory.createBalanceResponse(USERNAME_FROM, "1000.00"));

            assertThatExceptionOfType(UpstreamServiceException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, request))
                    .isEqualTo(depositFailure);

            verify(transferAccountService, times(1)).withdraw(USERNAME_FROM, new BigDecimal("250.00"));
            verify(transferAccountService, times(1)).deposit(USERNAME_TO, new BigDecimal("250.00"));
            verify(transferAccountService, times(1)).deposit(USERNAME_FROM, new BigDecimal("250.00"));
            verifyNoInteractions(transferNotificationService);
        }

        @Test
        @DisplayName("returns service unavailable when compensation also fails")
        void test7() {
            var request = TestDataFactory.createRequest("250.00");

            when(transferAccountService.withdraw(USERNAME_FROM, new BigDecimal("250.00")))
                    .thenReturn(TestDataFactory.createBalanceResponse(USERNAME_FROM, "750.00"));
            when(transferAccountService.deposit(USERNAME_TO, new BigDecimal("250.00")))
                    .thenThrow(new UpstreamServiceException("deposit failed"));
            when(transferAccountService.deposit(USERNAME_FROM, new BigDecimal("250.00")))
                    .thenThrow(new UpstreamServiceException("refund failed"));

            assertThatExceptionOfType(UpstreamServiceException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, request))
                    .withMessage("Transfer failed and compensation failed. Manual intervention required.");

            verify(transferAccountService, times(1)).withdraw(USERNAME_FROM, new BigDecimal("250.00"));
            verify(transferAccountService, times(1)).deposit(USERNAME_TO, new BigDecimal("250.00"));
            verify(transferAccountService, times(1)).deposit(USERNAME_FROM, new BigDecimal("250.00"));
            verifyNoInteractions(transferNotificationService);
        }
    }
}
