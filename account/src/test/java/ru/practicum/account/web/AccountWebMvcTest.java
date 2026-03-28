package ru.practicum.account.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.account.api.internalapi.AccountInternalApiController;
import ru.practicum.account.api.publicapi.AccountApiController;
import ru.practicum.account.domain.exception.AccountNotFoundException;
import ru.practicum.account.domain.exception.InsufficientFundsException;
import ru.practicum.account.domain.internalapi.BalanceResponse;
import ru.practicum.account.domain.internalapi.MoneyAmountRequest;
import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.publicapi.RecipientItem;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;
import ru.practicum.account.domain.publicapi.UpdateAccountRequest;
import ru.practicum.account.security.CurrentUsernameProvider;
import ru.practicum.account.service.AccountService;
import ru.practicum.account.web.advice.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        AccountApiController.class,
        AccountInternalApiController.class
}, excludeAutoConfiguration = {
        org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration.class,
        org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import({
        AccountDelegateImpl.class,
        AccountInternalDelegateImpl.class,
        GlobalExceptionHandler.class
})
@DisplayName("Account WebMvc")
class AccountWebMvcTest {

    private static final String USERNAME = "ivanivanov";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CurrentUsernameProvider currentUsernameProvider;

    @Nested
    @DisplayName("Public API")
    class PublicApi {

        @Test
        @DisplayName("get current account")
        void test1() throws Exception {
            var response = new AccountResponse(
                    USERNAME,
                    "Ivan Ivanov",
                    LocalDate.of(2001, 5, 10),
                    new BigDecimal("1000.00")
            );

            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME);
            when(accountService.getCurrentAccount(USERNAME)).thenReturn(response);

            mockMvc.perform(get("/api/v1/accounts/me"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.fullName").value("Ivan Ivanov"))
                    .andExpect(jsonPath("$.balance").value(1000.00));

            verify(accountService, times(1)).getCurrentAccount(USERNAME);
        }

        @Test
        @DisplayName("get recipients")
        void test2() throws Exception {
            var recipients = new RecipientPageResponse(
                    List.of(
                            new RecipientItem("petrpetrov", "Petr Petrov"),
                            new RecipientItem("annaivanova", "Anna Ivanova")
                    ),
                    0,
                    2,
                    2L,
                    1,
                    true
            );

            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME);
            when(accountService.getRecipients(USERNAME, 0, 2, null)).thenReturn(recipients);

            mockMvc.perform(get("/api/v1/accounts/recipients")
                            .param("page", "0")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].username").value("petrpetrov"));

            verify(accountService, times(1)).getRecipients(USERNAME, 0, 2, null);
        }

        @Test
        @DisplayName("get recipients validation error")
        void test3() throws Exception {
            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME);

            mockMvc.perform(get("/api/v1/accounts/recipients")
                            .param("page", "0")
                            .param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

            verify(accountService, never()).getRecipients(anyString(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("update current account")
        void test4() throws Exception {
            var response = new AccountResponse(
                    USERNAME,
                    "Ivan Sidorov",
                    LocalDate.of(2000, 1, 1),
                    new BigDecimal("1200.00")
            );

            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME);
            when(accountService.updateCurrentAccount(eq(USERNAME), any(UpdateAccountRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/v1/accounts/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fullName\":\"Ivan Sidorov\",\"dateOfBirth\":\"2000-01-01\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.fullName").value("Ivan Sidorov"));

            verify(accountService, times(1)).updateCurrentAccount(eq(USERNAME), any(UpdateAccountRequest.class));
        }

        @Test
        @DisplayName("update current account validation error")
        void test5() throws Exception {
            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME);

            mockMvc.perform(put("/api/v1/accounts/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fullName\":\"Ivan Sidorov\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

            verify(accountService, never()).updateCurrentAccount(anyString(), any());
        }

        @Test
        @DisplayName("account not found mapped to 404")
        void test6() throws Exception {
            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME);
            when(accountService.getCurrentAccount(USERNAME)).thenThrow(new AccountNotFoundException(USERNAME));

            mockMvc.perform(get("/api/v1/accounts/me"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("Internal API")
    class InternalApi {

        @Test
        @DisplayName("internal deposit")
        void test1() throws Exception {
            var request = new MoneyAmountRequest(new BigDecimal("250.00"));
            var response = new BalanceResponse(USERNAME, new BigDecimal("1250.00"));

            when(accountService.deposit(eq(USERNAME), any(MoneyAmountRequest.class))).thenReturn(response);

            mockMvc.perform(post("/internal/v1/accounts/{username}/deposit", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\":250.00}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.balance").value(1250.00));

            verify(accountService, times(1)).deposit(eq(USERNAME), any(MoneyAmountRequest.class));
        }

        @Test
        @DisplayName("internal withdraw insufficient funds mapped to 409")
        void test2() throws Exception {
            var request = new MoneyAmountRequest(new BigDecimal("5000.00"));

            when(accountService.withdraw(eq(USERNAME), any(MoneyAmountRequest.class)))
                    .thenThrow(new InsufficientFundsException(USERNAME));

            mockMvc.perform(post("/internal/v1/accounts/{username}/withdraw", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\":5000.00}"))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
        }

        @Test
        @DisplayName("internal deposit validation error")
        void test3() throws Exception {
            mockMvc.perform(post("/internal/v1/accounts/{username}/deposit", "usr")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\":100}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

            verify(accountService, never()).deposit(anyString(), any());
        }
    }
}
