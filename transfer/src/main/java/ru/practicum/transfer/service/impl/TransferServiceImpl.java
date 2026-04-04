package ru.practicum.transfer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.transfer.domain.TransferRequest;
import ru.practicum.transfer.domain.TransferResponse;
import ru.practicum.transfer.domain.exception.AccountNotFoundException;
import ru.practicum.transfer.domain.exception.InsufficientFundsException;
import ru.practicum.transfer.domain.exception.InvalidAmountException;
import ru.practicum.transfer.domain.exception.InvalidTransferRequestException;
import ru.practicum.transfer.domain.exception.InvalidUsernameException;
import ru.practicum.transfer.domain.exception.UpstreamServiceException;
import ru.practicum.transfer.domain.model.TransferEntity;
import ru.practicum.transfer.integration.account.service.TransferAccountService;
import ru.practicum.transfer.service.TransferService;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final TransferAccountService transferAccountService;
    private final TransferPersistenceService transferPersistenceService;

    @Override
    public TransferResponse transfer(String usernameFrom, TransferRequest request) {
        validateUsername(usernameFrom);
        validateRequest(usernameFrom, request);

        String usernameTo = request.getUsernameTo();
        BigDecimal amount = request.getAmount();

        log.info("Выполнение перевода: usernameFrom={}, usernameTo={}, amount={}", usernameFrom, usernameTo, amount);

        TransferEntity transfer = transferPersistenceService.createPendingTransfer(usernameFrom, usernameTo, amount);
        try {
            transferAccountService.withdraw(usernameFrom, amount);
        } catch (RuntimeException withdrawException) {
            failTransferSafely(transfer.getId(), "Withdraw failed: " + withdrawException.getMessage());
            throw withdrawException;
        }
        try {
            transferAccountService.deposit(usernameTo, amount);
        } catch (AccountNotFoundException
                 | InvalidAmountException
                 | InsufficientFundsException
                 | UpstreamServiceException depositException) {
            try {
                transferPersistenceService.enqueueCompensation(transfer.getId(), depositException.getMessage());
            } catch (RuntimeException compensationScheduleException) {
                log.error("Не удалось запланировать компенсацию перевода: transferId={}", transfer.getId(), compensationScheduleException);
                throw new UpstreamServiceException("Transfer failed and compensation scheduling failed. Manual intervention required.");
            }
            throw new UpstreamServiceException(
                    "Transfer could not be completed. Compensation was scheduled asynchronously."
            );
        }
        transferPersistenceService.markCompletedAndEnqueueNotification(transfer.getId());

        log.info("Перевод выполнен: usernameFrom={}, usernameTo={}, amount={}", usernameFrom, usernameTo, amount);
        return new TransferResponse(usernameFrom, usernameTo, amount);
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUsernameException();
        }
    }

    private void validateRequest(String usernameFrom, TransferRequest request) {
        if (request == null || request.getUsernameTo() == null) {
            throw new InvalidTransferRequestException();
        }
        String usernameTo = request.getUsernameTo().trim();
        if (usernameTo.isEmpty()) {
            throw new InvalidTransferRequestException("usernameTo must not be blank");
        }
        if (usernameFrom.equals(usernameTo)) {
            throw new InvalidTransferRequestException("usernameTo must be different from usernameFrom");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
        request.setUsernameTo(usernameTo);
    }

    private void failTransferSafely(UUID transferId, String reason) {
        try {
            transferPersistenceService.markFailed(transferId, reason);
        } catch (RuntimeException markFailedException) {
            log.error("Не удалось перевести transfer в FAILED: transferId={}", transferId, markFailedException);
        }
    }
}
