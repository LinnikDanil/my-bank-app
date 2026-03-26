package ru.practicum.account.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import ru.practicum.account.api.publicapi.AccountApiDelegate;
import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;
import ru.practicum.account.domain.publicapi.UpdateAccountRequest;
import ru.practicum.account.security.CurrentUsernameProvider;
import ru.practicum.account.service.AccountService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountDelegateImpl implements AccountApiDelegate {

    private final AccountService accountService;
    private final CurrentUsernameProvider currentUsernameProvider;

    @Override
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<AccountResponse> getCurrentAccount() {
        String username = currentUsernameProvider.requireUsername();
        var response = accountService.getCurrentAccount(username);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<RecipientPageResponse> getRecipients(Integer page, Integer size, String search) {
        String username = currentUsernameProvider.requireUsername();
        var response = accountService.getRecipients(username, page, size, search);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<AccountResponse> updateCurrentAccount(UpdateAccountRequest updateAccountRequest) {
        String username = currentUsernameProvider.requireUsername();
        var response = accountService.updateCurrentAccount(username, updateAccountRequest);
        return ResponseEntity.ok(response);
    }
}
