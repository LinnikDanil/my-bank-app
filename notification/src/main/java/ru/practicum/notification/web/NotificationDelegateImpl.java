package ru.practicum.notification.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import ru.practicum.notification.api.NotificationInternalApiDelegate;
import ru.practicum.notification.domain.NotificationEvent;
import ru.practicum.notification.service.NotificationService;

@Component
@RequiredArgsConstructor
public class NotificationDelegateImpl implements NotificationInternalApiDelegate {

    private final NotificationService notificationService;

    @Override
    @PreAuthorize("hasAnyAuthority('ACCOUNT','CASH','TRANSFER')")
    public ResponseEntity<Void> sendNotificationEvent(NotificationEvent notificationEvent) {
        notificationService.processEvent(notificationEvent);
        return ResponseEntity.ok().build();
    }
}
