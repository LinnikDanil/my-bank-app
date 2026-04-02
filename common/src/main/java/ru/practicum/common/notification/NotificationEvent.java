package ru.practicum.common.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationEvent {
    private UUID eventId;
    private NotificationEventType eventType;
    private OffsetDateTime timestamp;
    private List<String> recipients;
    private NotificationEventPayload payload;
}
