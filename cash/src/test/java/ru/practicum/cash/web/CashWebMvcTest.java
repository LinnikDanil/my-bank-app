package ru.practicum.cash.web;

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
import ru.practicum.cash.api.CashApiController;
import ru.practicum.cash.domain.CashOperationResponse;
import ru.practicum.cash.domain.exception.InsufficientFundsException;
import ru.practicum.cash.security.CurrentUsernameProvider;
import ru.practicum.cash.service.CashService;
import ru.practicum.cash.web.advice.GlobalExceptionHandler;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        CashApiController.class
}, excludeAutoConfiguration = {
        org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration.class,
        org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import({
        CashDelegateImpl.class,
        GlobalExceptionHandler.class
})
@DisplayName("Cash WebMvc")
class CashWebMvcTest {

    private static final String USERNAME = "ivanivanov";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CashService cashService;

    @MockitoBean
    private CurrentUsernameProvider currentUsernameProvider;

    @Nested
    @DisplayName("API")
    class Api {

        @Test
        @DisplayName("deposit")
        void test1() throws Exception {
            var response = new CashOperationResponse(USERNAME, new BigDecimal("100.00"), new BigDecimal("1100.00"));

            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME);
            when(cashService.deposit(eq(USERNAME), any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/cash/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\":100.00}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.balance").value(1100.00));

            verify(cashService, times(1)).deposit(eq(USERNAME), any());
        }

        @Test
        @DisplayName("withdraw")
        void test2() throws Exception {
            var response = new CashOperationResponse(USERNAME, new BigDecimal("50.00"), new BigDecimal("950.00"));

            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME);
            when(cashService.withdraw(eq(USERNAME), any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/cash/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\":50.00}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.balance").value(950.00));

            verify(cashService, times(1)).withdraw(eq(USERNAME), any());
        }

        @Test
        @DisplayName("withdraw insufficient funds mapped to 409")
        void test3() throws Exception {
            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME);
            when(cashService.withdraw(eq(USERNAME), any())).thenThrow(new InsufficientFundsException(USERNAME));

            mockMvc.perform(post("/api/v1/cash/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\":5000.00}"))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
        }

        @Test
        @DisplayName("validation error")
        void test4() throws Exception {
            mockMvc.perform(post("/api/v1/cash/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

            verify(cashService, never()).deposit(anyString(), any());
        }
    }
}
