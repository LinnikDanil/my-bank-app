package ru.practicum.transfer.integration.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.transfer.integration.notification.api.NotificationInternalApi;
import ru.practicum.transfer.integration.notification.client.ApiClient;
import ru.practicum.transfer.integration.notification.domain.NotificationEvent;
import ru.practicum.transfer.integration.notification.domain.NotificationEventPayload;
import ru.practicum.transfer.integration.notification.domain.NotificationEventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("NotificationInternalApi")
class NotificationInternalApiIT {

    @Test
    @DisplayName("sendNotificationEvent")
    void test1() {
        var builder = ApiClient.buildRestClientBuilder();
        var server = MockRestServiceServer.bindTo(builder).build();

        var apiClient = new ApiClient(builder.build());
        apiClient.setBasePath("http://notification");
        var api = new NotificationInternalApi(apiClient);

        server.expect(requestTo("http://notification/internal/v1/notifications/events"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        NotificationEvent event = new NotificationEvent()
                .eventId(UUID.randomUUID())
                .eventType(NotificationEventType.TRANSFER_COMPLETED)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .recipients(List.of("ivanivanov", "petrpetrov"))
                .payload(new NotificationEventPayload()
                        .usernameFrom("ivanivanov")
                        .usernameTo("petrpetrov")
                        .amount(new BigDecimal("100.00")));

        api.sendNotificationEvent(event);
        server.verify();
    }
}
