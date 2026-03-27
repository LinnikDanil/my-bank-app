package ru.practicum.transfer.util;

import ru.practicum.transfer.domain.TransferRequest;
import ru.practicum.transfer.domain.TransferResponse;
import ru.practicum.transfer.integration.account.domain.BalanceResponse;

import java.math.BigDecimal;

public final class TestDataFactory {

    public static final String USERNAME_FROM = "ivanivanov";
    public static final String USERNAME_TO = "petrpetrov";

    private TestDataFactory() {
    }

    public static TransferRequest createRequest(String amount) {
        return new TransferRequest(USERNAME_TO, new BigDecimal(amount));
    }

    public static TransferResponse createResponse(String amount) {
        return new TransferResponse(USERNAME_FROM, USERNAME_TO, new BigDecimal(amount));
    }

    public static BalanceResponse createBalanceResponse(String username, String balance) {
        return new BalanceResponse()
                .username(username)
                .balance(new BigDecimal(balance));
    }
}
