package ru.practicum.front.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    private final ApiClientProperties properties;
    private final OAuth2AuthorizedClientService authorizedClientService;
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
        ru.practicum.front.integration.account.client.ApiClient client =
                new ru.practicum.front.integration.account.client.ApiClient()
                        .setBasePath(properties.accountBaseUrl());
        client.setBearerToken(resolveAccessToken());
        return call.execute(new AccountApi(client));
    }

    private <T> T withCashApi(ApiCall<T, CashApi> call) {
        ru.practicum.front.integration.cash.client.ApiClient client =
                new ru.practicum.front.integration.cash.client.ApiClient()
                        .setBasePath(properties.cashBaseUrl());
        client.setBearerToken(resolveAccessToken());
        return call.execute(new CashApi(client));
    }

    private <T> T withTransferApi(ApiCall<T, TransferApi> call) {
        ru.practicum.front.integration.transfer.client.ApiClient client =
                new ru.practicum.front.integration.transfer.client.ApiClient()
                        .setBasePath(properties.transferBaseUrl());
        client.setBearerToken(resolveAccessToken());
        return call.execute(new TransferApi(client));
    }

    private String resolveAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
            throw new IllegalStateException("OAuth2 authentication is required");
        }

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauth2Token.getAuthorizedClientRegistrationId(),
                oauth2Token.getName()
        );

        if (client == null || client.getAccessToken() == null || client.getAccessToken().getTokenValue() == null) {
            throw new IllegalStateException("Access token is missing");
        }
        return client.getAccessToken().getTokenValue();
    }

    @FunctionalInterface
    private interface ApiCall<T, A> {
        T execute(A api);
    }
}
