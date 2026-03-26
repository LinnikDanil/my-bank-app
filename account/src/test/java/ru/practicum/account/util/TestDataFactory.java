package ru.practicum.account.util;

import ru.practicum.account.domain.internalapi.MoneyAmountRequest;
import ru.practicum.account.domain.model.Account;
import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;
import ru.practicum.account.domain.publicapi.UpdateAccountRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class TestDataFactory {

    public static final String USERNAME = "ivanivanov";

    private TestDataFactory() {
    }

    public static Account createAccount(String username, String fullName, LocalDate dateOfBirth, BigDecimal balance) {
        return Account.builder()
                .username(username)
                .fullName(fullName)
                .dateOfBirth(dateOfBirth)
                .balance(balance)
                .build();
    }

    public static Account createDefaultAccount() {
        return createAccount(USERNAME, "Ivan Ivanov", LocalDate.of(2001, 5, 10), new BigDecimal("1000.00"));
    }

    public static UpdateAccountRequest createAdultUpdateRequest() {
        return new UpdateAccountRequest("Ivan Sidorov", LocalDate.now().minusYears(20));
    }

    public static UpdateAccountRequest createMinorUpdateRequest() {
        return new UpdateAccountRequest("Ivan Sidorov", LocalDate.now().minusYears(17));
    }

    public static MoneyAmountRequest createMoneyAmountRequest(String amount) {
        return new MoneyAmountRequest(new BigDecimal(amount));
    }

    public static AccountResponse createAccountResponse(Account account) {
        return new AccountResponse(
                account.getUsername(),
                account.getFullName(),
                account.getDateOfBirth(),
                account.getBalance()
        );
    }

    public static RecipientPageResponse emptyRecipientPageResponse(int page, int size) {
        return new RecipientPageResponse(List.of(), page, size, 0L, 0, true);
    }
}
