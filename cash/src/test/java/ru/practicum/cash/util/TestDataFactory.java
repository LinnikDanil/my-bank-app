package ru.practicum.cash.util;

import ru.practicum.cash.domain.CashOperationRequest;
import ru.practicum.cash.domain.CashOperationResponse;
import ru.practicum.cash.integration.account.domain.BalanceResponse;

import java.math.BigDecimal;

public final class TestDataFactory {

    public static final String USERNAME = "ivanivanov";

    private TestDataFactory() {
    }

    public static CashOperationRequest createRequest(String amount) {
        return new CashOperationRequest(new BigDecimal(amount));
    }

    public static BalanceResponse createBalanceResponse(String balance) {
        return new BalanceResponse()
                .username(USERNAME)
                .balance(new BigDecimal(balance));
    }

    public static CashOperationResponse createOperationResponse(String amount, String balance) {
        return new CashOperationResponse(USERNAME, new BigDecimal(amount), new BigDecimal(balance));
    }
}
