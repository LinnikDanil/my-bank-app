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
import ru.practicum.transfer.domain.model.TransferEntity;
import ru.practicum.transfer.integration.account.service.TransferAccountService;
import ru.practicum.transfer.util.TestDataFactory;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferServiceImpl")
class TransferServiceImplTest {

    private static final String USERNAME_FROM = TestDataFactory.USERNAME_FROM;
    private static final String USERNAME_TO = TestDataFactory.USERNAME_TO;

    @Mock
    private TransferAccountService transferAccountService;

    @Mock
    private TransferPersistenceService transferPersistenceService;

    @InjectMocks
    private TransferServiceImpl transferService;

    @Nested
    @DisplayName("transfer")
    class Transfer {

        @Test
        @DisplayName("ok")
        void transfer_returnsResponseAndEnqueuesNotificationOutboxEvent() {
            var request = TestDataFactory.createRequest("250.00");
            TransferEntity transfer = TransferEntity.newPending(USERNAME_FROM, USERNAME_TO, new BigDecimal("250.00"));
            transfer.setId(UUID.randomUUID());

            when(transferPersistenceService.createPendingTransfer(USERNAME_FROM, USERNAME_TO, new BigDecimal("250.00")))
                    .thenReturn(transfer);
            when(transferAccountService.withdraw(USERNAME_FROM, new BigDecimal("250.00")))
                    .thenReturn(TestDataFactory.createBalanceResponse(USERNAME_FROM, "750.00"));
            when(transferAccountService.deposit(USERNAME_TO, new BigDecimal("250.00")))
                    .thenReturn(TestDataFactory.createBalanceResponse(USERNAME_TO, "1250.00"));

            var result = transferService.transfer(USERNAME_FROM, request);

            assertThat(result.getUsernameFrom()).isEqualTo(USERNAME_FROM);
            assertThat(result.getUsernameTo()).isEqualTo(USERNAME_TO);
            assertThat(result.getAmount()).isEqualByComparingTo("250.00");
            verify(transferPersistenceService).markCompletedAndEnqueueNotification(transfer.getId());
            verify(transferPersistenceService, never()).enqueueCompensation(any(), anyString());
        }

        @Test
        @DisplayName("invalid username")
        void transfer_throwsInvalidUsernameException_whenUsernameIsBlank() {
            assertThatExceptionOfType(InvalidUsernameException.class)
                    .isThrownBy(() -> transferService.transfer(" ", TestDataFactory.createRequest("10.00")));

            verifyNoInteractions(transferAccountService, transferPersistenceService);
        }

        @Test
        @DisplayName("request is null")
        void transfer_throwsInvalidTransferRequestException_whenRequestIsNull() {
            assertThatExceptionOfType(InvalidTransferRequestException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, null));

            verifyNoInteractions(transferAccountService, transferPersistenceService);
        }

        @Test
        @DisplayName("amount must be positive")
        void transfer_throwsInvalidAmountException_whenAmountIsNotPositive() {
            assertThatExceptionOfType(InvalidAmountException.class)
                    .isThrownBy(() -> transferService.transfer(
                            USERNAME_FROM,
                            new ru.practicum.transfer.domain.TransferRequest(USERNAME_TO, BigDecimal.ZERO)
                    ));

            verifyNoInteractions(transferAccountService, transferPersistenceService);
        }

        @Test
        @DisplayName("usernameTo must be different")
        void transfer_throwsInvalidTransferRequestException_whenRecipientMatchesSender() {
            var request = new ru.practicum.transfer.domain.TransferRequest(USERNAME_FROM, new BigDecimal("10.00"));

            assertThatExceptionOfType(InvalidTransferRequestException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, request));

            verifyNoInteractions(transferAccountService, transferPersistenceService);
        }

        @Test
        @DisplayName("creates async compensation when deposit to recipient fails")
        void transfer_enqueuesCompensationOutboxEvent_whenRecipientDepositFails() {
            var request = TestDataFactory.createRequest("250.00");
            TransferEntity transfer = TransferEntity.newPending(USERNAME_FROM, USERNAME_TO, new BigDecimal("250.00"));
            transfer.setId(UUID.randomUUID());

            when(transferPersistenceService.createPendingTransfer(USERNAME_FROM, USERNAME_TO, new BigDecimal("250.00")))
                    .thenReturn(transfer);
            when(transferAccountService.withdraw(USERNAME_FROM, new BigDecimal("250.00")))
                    .thenReturn(TestDataFactory.createBalanceResponse(USERNAME_FROM, "750.00"));
            when(transferAccountService.deposit(USERNAME_TO, new BigDecimal("250.00")))
                    .thenThrow(new UpstreamServiceException("deposit failed"));

            assertThatExceptionOfType(UpstreamServiceException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, request))
                    .withMessageContaining("Compensation was scheduled asynchronously");

            verify(transferPersistenceService).enqueueCompensation(eq(transfer.getId()), eq("deposit failed"));
            verify(transferPersistenceService, never()).markCompletedAndEnqueueNotification(any());
        }

        @Test
        @DisplayName("marks transfer failed when withdraw fails")
        void transfer_marksFailed_whenWithdrawFails() {
            var request = TestDataFactory.createRequest("250.00");
            TransferEntity transfer = TransferEntity.newPending(USERNAME_FROM, USERNAME_TO, new BigDecimal("250.00"));
            transfer.setId(UUID.randomUUID());

            when(transferPersistenceService.createPendingTransfer(USERNAME_FROM, USERNAME_TO, new BigDecimal("250.00")))
                    .thenReturn(transfer);
            when(transferAccountService.withdraw(USERNAME_FROM, new BigDecimal("250.00")))
                    .thenThrow(new UpstreamServiceException("withdraw failed"));

            assertThatExceptionOfType(UpstreamServiceException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, request))
                    .withMessage("withdraw failed");

            verify(transferPersistenceService).markFailed(eq(transfer.getId()), eq("Withdraw failed: withdraw failed"));
            verify(transferAccountService, never()).deposit(anyString(), any());
            verify(transferPersistenceService, never()).enqueueCompensation(any(), anyString());
            verify(transferPersistenceService, never()).markCompletedAndEnqueueNotification(any());
        }

        @Test
        @DisplayName("does not create compensation for unexpected runtime exception")
        void transfer_rethrowsUnexpectedRuntimeExceptionWithoutCompensation() {
            var request = TestDataFactory.createRequest("250.00");
            TransferEntity transfer = TransferEntity.newPending(USERNAME_FROM, USERNAME_TO, new BigDecimal("250.00"));
            transfer.setId(UUID.randomUUID());

            when(transferPersistenceService.createPendingTransfer(USERNAME_FROM, USERNAME_TO, new BigDecimal("250.00")))
                    .thenReturn(transfer);
            when(transferAccountService.withdraw(USERNAME_FROM, new BigDecimal("250.00")))
                    .thenReturn(TestDataFactory.createBalanceResponse(USERNAME_FROM, "750.00"));
            when(transferAccountService.deposit(USERNAME_TO, new BigDecimal("250.00")))
                    .thenThrow(new IllegalStateException("unexpected"));

            assertThatExceptionOfType(IllegalStateException.class)
                    .isThrownBy(() -> transferService.transfer(USERNAME_FROM, request))
                    .withMessage("unexpected");

            verify(transferPersistenceService, never()).enqueueCompensation(any(), anyString());
            verify(transferPersistenceService, never()).markCompletedAndEnqueueNotification(any());
        }
    }
}
