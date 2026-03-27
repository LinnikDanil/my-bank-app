package ru.practicum.transfer.web;

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
import ru.practicum.transfer.api.TransferApiController;
import ru.practicum.transfer.domain.TransferResponse;
import ru.practicum.transfer.domain.exception.InsufficientFundsException;
import ru.practicum.transfer.security.CurrentUsernameProvider;
import ru.practicum.transfer.service.TransferService;
import ru.practicum.transfer.web.advice.GlobalExceptionHandler;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        TransferApiController.class
}, excludeAutoConfiguration = {
        org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import({
        TransferDelegateImpl.class,
        GlobalExceptionHandler.class
})
@DisplayName("Transfer WebMvc")
class TransferWebMvcTest {

    private static final String USERNAME_FROM = "ivanivanov";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private CurrentUsernameProvider currentUsernameProvider;

    @Nested
    @DisplayName("API")
    class Api {

        @Test
        @DisplayName("create transfer")
        void test1() throws Exception {
            var response = new TransferResponse(USERNAME_FROM, "petrpetrov", new BigDecimal("250.00"));

            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME_FROM);
            when(transferService.transfer(eq(USERNAME_FROM), any())).thenReturn(response);

            mockMvc.perform(post("/api/v1/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"usernameTo\":\"petrpetrov\",\"amount\":250.00}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.usernameFrom").value(USERNAME_FROM))
                    .andExpect(jsonPath("$.usernameTo").value("petrpetrov"))
                    .andExpect(jsonPath("$.amount").value(250.00));

            verify(transferService, times(1)).transfer(eq(USERNAME_FROM), any());
        }

        @Test
        @DisplayName("insufficient funds mapped to 409")
        void test2() throws Exception {
            when(currentUsernameProvider.requireUsername()).thenReturn(USERNAME_FROM);
            when(transferService.transfer(eq(USERNAME_FROM), any())).thenThrow(new InsufficientFundsException(USERNAME_FROM));

            mockMvc.perform(post("/api/v1/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"usernameTo\":\"petrpetrov\",\"amount\":5000.00}"))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
        }

        @Test
        @DisplayName("validation error")
        void test3() throws Exception {
            mockMvc.perform(post("/api/v1/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

            verify(transferService, never()).transfer(anyString(), any());
        }
    }
}
