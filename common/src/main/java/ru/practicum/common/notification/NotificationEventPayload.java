package ru.practicum.common.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationEventPayload {
    private String username;
    private String fullName;
    private LocalDate dateOfBirth;
    private String usernameFrom;
    private String usernameTo;
    private BigDecimal amount;
}
