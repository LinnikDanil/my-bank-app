package ru.practicum.notification.web;

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
import ru.practicum.notification.api.NotificationInternalApiController;
import ru.practicum.notification.service.NotificationService;
import ru.practicum.notification.web.advice.GlobalExceptionHandler;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        NotificationInternalApiController.class
}, excludeAutoConfiguration = {
        org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import({
        NotificationDelegateImpl.class,
        GlobalExceptionHandler.class
})
@DisplayName("Notification WebMvc")
class NotificationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Nested
    @DisplayName("Internal API")
    class InternalApi {

        @Test
        @DisplayName("accepts event")
        void test1() throws Exception {
            String body = """
                    {
                      "eventId":"550e8400-e29b-41d4-a716-446655440000",
                      "eventType":"CASH_DEPOSIT",
                      "timestamp":"2026-03-19T10:15:30Z",
                      "recipients":["ivanivanov"],
                      "payload":{
                        "username":"ivanivanov",
                        "amount":100.00
                      }
                    }
                    """;

            mockMvc.perform(post("/internal/v1/notifications/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());

            verify(notificationService, times(1)).processEvent(any());
        }

        @Test
        @DisplayName("validation error")
        void test2() throws Exception {
            mockMvc.perform(post("/internal/v1/notifications/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

            verify(notificationService, never()).processEvent(any());
        }
    }
}
