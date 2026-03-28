package ru.practicum.transfer.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import ru.practicum.transfer.api.TransferApiDelegate;
import ru.practicum.transfer.domain.TransferRequest;
import ru.practicum.transfer.domain.TransferResponse;
import ru.practicum.transfer.security.CurrentUsernameProvider;
import ru.practicum.transfer.service.TransferService;

@Component
@RequiredArgsConstructor
public class TransferDelegateImpl implements TransferApiDelegate {

    private final TransferService transferService;
    private final CurrentUsernameProvider currentUsernameProvider;

    @Override
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<TransferResponse> createTransfer(TransferRequest transferRequest) {
        String username = currentUsernameProvider.requireUsername();
        var response = transferService.transfer(username, transferRequest);
        return ResponseEntity.ok(response);
    }
}
