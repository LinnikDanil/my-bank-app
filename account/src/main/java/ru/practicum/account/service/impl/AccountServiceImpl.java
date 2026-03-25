package ru.practicum.account.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.account.domain.exception.AccountNotFoundException;
import ru.practicum.account.domain.exception.InsufficientFundsException;
import ru.practicum.account.domain.exception.InvalidAmountException;
import ru.practicum.account.domain.exception.InvalidMoneyAmountRequestException;
import ru.practicum.account.domain.exception.InvalidPaginationException;
import ru.practicum.account.domain.exception.InvalidUpdateAccountRequestException;
import ru.practicum.account.domain.exception.InvalidUsernameException;
import ru.practicum.account.domain.model.Account;
import ru.practicum.account.domain.internalapi.BalanceResponse;
import ru.practicum.account.domain.internalapi.MoneyAmountRequest;
import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;
import ru.practicum.account.domain.publicapi.UpdateAccountRequest;
import ru.practicum.account.integration.notification.service.AccountNotificationService;
import ru.practicum.account.repository.AccountRepository;
import ru.practicum.account.service.AccountService;
import ru.practicum.account.service.mapper.AccountMapper;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private static final String CURRENT_USERNAME = "todo";

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountNotificationService accountNotificationService;

    @Override
    public AccountResponse getCurrentAccount() {
        Account account = findRequiredAccount(CURRENT_USERNAME);
        return accountMapper.toAccountResponse(account);
    }

    @Override
    public RecipientPageResponse getRecipients(Integer page, Integer size, String search) {
        validatePagination(page, size);
        var pageable = PageRequest.of(page, size);
        String normalizedSearch = normalizeSearch(search);
        Page<Account> accountPage = accountRepository.findRecipients(CURRENT_USERNAME, normalizedSearch, pageable);
        return accountMapper.toRecipientPageResponse(accountPage);
    }

    @Override
    public AccountResponse updateCurrentAccount(UpdateAccountRequest updateAccountRequest) {
        validateUpdateRequest(updateAccountRequest);
        Account account = findRequiredAccount(CURRENT_USERNAME);
        account.setFullName(updateAccountRequest.getFullName());
        account.setDateOfBirth(updateAccountRequest.getDateOfBirth());
        Account saved = accountRepository.save(account);
        accountNotificationService.notifyAccountUpdated(saved);
        return accountMapper.toAccountResponse(saved);
    }

    @Override
    public BalanceResponse deposit(String username, MoneyAmountRequest moneyAmountRequest) {
        validateUsername(username);
        validateMoneyAmountRequest(moneyAmountRequest);
        Account account = findRequiredAccount(username);
        BigDecimal amount = moneyAmountRequest.getAmount();
        account.setBalance(account.getBalance().add(amount));
        Account saved = accountRepository.save(account);
        accountNotificationService.notifyCashDeposit(username, amount);
        return accountMapper.toBalanceResponse(saved);
    }

    @Override
    public BalanceResponse withdraw(String username, MoneyAmountRequest moneyAmountRequest) {
        validateUsername(username);
        validateMoneyAmountRequest(moneyAmountRequest);
        Account account = findRequiredAccount(username);
        BigDecimal amount = moneyAmountRequest.getAmount();
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(username);
        }
        account.setBalance(account.getBalance().subtract(amount));
        Account saved = accountRepository.save(account);
        accountNotificationService.notifyCashWithdraw(username, amount);
        return accountMapper.toBalanceResponse(saved);
    }

    private Account findRequiredAccount(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new AccountNotFoundException(username));
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validatePagination(Integer page, Integer size) {
        if (page == null || size == null || page < 0 || size <= 0) {
            throw new InvalidPaginationException();
        }
    }

    private void validateUpdateRequest(UpdateAccountRequest updateAccountRequest) {
        if (updateAccountRequest == null
                || updateAccountRequest.getFullName() == null
                || updateAccountRequest.getDateOfBirth() == null) {
            throw new InvalidUpdateAccountRequestException();
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUsernameException();
        }
    }

    private void validateMoneyAmountRequest(MoneyAmountRequest moneyAmountRequest) {
        if (moneyAmountRequest == null) {
            throw new InvalidMoneyAmountRequestException();
        }
        if (moneyAmountRequest.getAmount() == null || moneyAmountRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
    }
}
