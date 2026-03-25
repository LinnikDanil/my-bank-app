package ru.practicum.account.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.account.api.publicapi.AccountApiDelegate;
import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;
import ru.practicum.account.domain.publicapi.UpdateAccountRequest;
import ru.practicum.account.service.AccountService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountDelegateImpl implements AccountApiDelegate {

    private final AccountService accountService;

    @Override
    public ResponseEntity<AccountResponse> getCurrentAccount() {
        var response = accountService.getCurrentAccount();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RecipientPageResponse> getRecipients(Integer page, Integer size, String search) {
        var response = accountService.getRecipients(page, size, search);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AccountResponse> updateCurrentAccount(UpdateAccountRequest updateAccountRequest) {
        var response = accountService.updateCurrentAccount(updateAccountRequest);
        return ResponseEntity.ok(response);    }
}
