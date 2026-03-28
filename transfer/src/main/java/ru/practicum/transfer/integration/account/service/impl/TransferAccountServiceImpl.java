package ru.practicum.transfer.integration.account.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.transfer.domain.exception.AccountNotFoundException;
import ru.practicum.transfer.domain.exception.InsufficientFundsException;
import ru.practicum.transfer.domain.exception.InvalidAmountException;
import ru.practicum.transfer.domain.exception.UpstreamServiceException;
import ru.practicum.transfer.integration.account.api.AccountInternalApi;
import ru.practicum.transfer.integration.account.domain.BalanceResponse;
import ru.practicum.transfer.integration.account.domain.MoneyAmountRequest;
import ru.practicum.transfer.integration.account.service.TransferAccountService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferAccountServiceImpl implements TransferAccountService {

    private final AccountInternalApi accountInternalApi;

    @Override
    @CircuitBreaker(name = "accountService")
    @Retry(name = "accountService")
    public BalanceResponse deposit(String username, BigDecimal amount) {
        try {
            return accountInternalApi.internalDeposit(username, new MoneyAmountRequest().amount(amount));
        } catch (RestClientResponseException ex) {
            throw mapException(username, ex);
        } catch (Exception ex) {
            throw new UpstreamServiceException("Account service is unavailable");
        }
    }

    @Override
    @CircuitBreaker(name = "accountService")
    @Retry(name = "accountService")
    public BalanceResponse withdraw(String username, BigDecimal amount) {
        try {
            return accountInternalApi.internalWithdraw(username, new MoneyAmountRequest().amount(amount));
        } catch (RestClientResponseException ex) {
            throw mapException(username, ex);
        } catch (Exception ex) {
            throw new UpstreamServiceException("Account service is unavailable");
        }
    }

    private RuntimeException mapException(String username, RestClientResponseException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            log.error("Неизвестный статус ответа account-service: {}", ex.getStatusCode(), ex);
            return new UpstreamServiceException("Account service returned unexpected status");
        }

        return switch (status) {
            case BAD_REQUEST -> new InvalidAmountException();
            case NOT_FOUND -> new AccountNotFoundException(username);
            case CONFLICT -> new InsufficientFundsException(username);
            case SERVICE_UNAVAILABLE -> new UpstreamServiceException("Account service is unavailable");
            default -> {
                log.error("Ошибка account-service: status={}, body={}", status, ex.getResponseBodyAsString(), ex);
                yield new UpstreamServiceException("Account service operation failed");
            }
        };
    }
}
