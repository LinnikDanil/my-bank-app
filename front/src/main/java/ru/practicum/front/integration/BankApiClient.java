package ru.practicum.front.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.front.integration.account.api.AccountApi;
import ru.practicum.front.integration.account.domain.AccountResponse;
import ru.practicum.front.integration.account.domain.RecipientPageResponse;
import ru.practicum.front.integration.account.domain.UpdateAccountRequest;
import ru.practicum.front.integration.cash.api.CashApi;
import ru.practicum.front.integration.cash.domain.CashOperationRequest;
import ru.practicum.front.integration.transfer.api.TransferApi;
import ru.practicum.front.integration.transfer.domain.TransferRequest;
import ru.practicum.front.integration.transfer.domain.TransferResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class BankApiClient {

    private final AccountApi accountApi;
    private final CashApi cashApi;
    private final TransferApi transferApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AccountResponse getCurrentAccount() {
        return withAccountApi(AccountApi::getCurrentAccount);
    }

    public RecipientPageResponse getRecipients(int page, int size, String search) {
        return withAccountApi(api -> api.getRecipients(page, size, search));
    }

    public void updateCurrentAccount(String fullName, LocalDate dateOfBirth) {
        UpdateAccountRequest request = new UpdateAccountRequest()
                .fullName(fullName)
                .dateOfBirth(dateOfBirth);
        withAccountApi(api -> api.updateCurrentAccount(request));
    }

    public void deposit(BigDecimal amount) {
        CashOperationRequest request = new CashOperationRequest().amount(amount);
        withCashApi(api -> api.depositCash(request));
    }

    public void withdraw(BigDecimal amount) {
        CashOperationRequest request = new CashOperationRequest().amount(amount);
        withCashApi(api -> api.withdrawCash(request));
    }

    public TransferResponse transfer(String usernameTo, BigDecimal amount) {
        TransferRequest request = new TransferRequest()
                .usernameTo(usernameTo)
                .amount(amount);
        return withTransferApi(api -> api.createTransfer(request));
    }

    public String extractErrorMessage(RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        if (body.isBlank()) {
            return "Ошибка запроса: HTTP " + ex.getStatusCode().value();
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            String message = root.path("message").asText("");
            if (!message.isBlank()) {
                return message;
            }
        } catch (Exception ignored) {
            // No-op
        }
        return "Ошибка запроса: HTTP " + ex.getStatusCode().value();
    }

    private <T> T withAccountApi(ApiCall<T, AccountApi> call) {
        return call.execute(accountApi);
    }

    private <T> T withCashApi(ApiCall<T, CashApi> call) {
        return call.execute(cashApi);
    }

    private <T> T withTransferApi(ApiCall<T, TransferApi> call) {
        return call.execute(transferApi);
    }

    @FunctionalInterface
    private interface ApiCall<T, A> {
        T execute(A api);
    }
}
