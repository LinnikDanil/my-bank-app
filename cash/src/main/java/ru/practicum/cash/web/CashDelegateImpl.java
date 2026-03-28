package ru.practicum.cash.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import ru.practicum.cash.api.CashApiDelegate;
import ru.practicum.cash.domain.CashOperationRequest;
import ru.practicum.cash.domain.CashOperationResponse;
import ru.practicum.cash.security.CurrentUsernameProvider;
import ru.practicum.cash.service.CashService;

@Component
@RequiredArgsConstructor
public class CashDelegateImpl implements CashApiDelegate {

    private final CashService cashService;
    private final CurrentUsernameProvider currentUsernameProvider;

    @Override
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<CashOperationResponse> depositCash(CashOperationRequest cashOperationRequest) {
        String username = currentUsernameProvider.requireUsername();
        var response = cashService.deposit(username, cashOperationRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<CashOperationResponse> withdrawCash(CashOperationRequest cashOperationRequest) {
        String username = currentUsernameProvider.requireUsername();
        var response = cashService.withdraw(username, cashOperationRequest);
        return ResponseEntity.ok(response);
    }
}
