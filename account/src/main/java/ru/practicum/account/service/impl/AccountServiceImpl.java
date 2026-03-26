package ru.practicum.account.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private static final int MIN_AGE_YEARS = 18;

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final AccountNotificationService accountNotificationService;

    @Override
    public AccountResponse getCurrentAccount(String username) {
        validateUsername(username);
        log.info("Получение текущего аккаунта");
        Account account = findRequiredAccount(username);
        log.info("Текущий аккаунт найден: username={}", account.getUsername());
        return accountMapper.toAccountResponse(account);
    }

    @Override
    public RecipientPageResponse getRecipients(String username, Integer page, Integer size, String search) {
        validateUsername(username);
        validatePagination(page, size);
        log.info("Запрошен список получателей: page={}, size={}, search={}", page, size, search);
        var pageable = PageRequest.of(page, size);
        String normalizedSearch = normalizeSearch(search);
        Page<Account> accountPage = accountRepository.findRecipients(username, normalizedSearch, pageable);
        log.info("Список получателей сформирован: elements={}, totalPages={}",
                accountPage.getNumberOfElements(), accountPage.getTotalPages());
        return accountMapper.toRecipientPageResponse(accountPage);
    }

    @Override
    public AccountResponse updateCurrentAccount(String username, UpdateAccountRequest updateAccountRequest) {
        validateUsername(username);
        validateUpdateRequest(updateAccountRequest);
        log.info("Обновление текущего аккаунта");
        Account account = findRequiredAccount(username);
        account.setFullName(updateAccountRequest.getFullName());
        account.setDateOfBirth(updateAccountRequest.getDateOfBirth());
        Account saved = accountRepository.save(account);
        log.info("Аккаунт обновлен: username={}", saved.getUsername());
        accountNotificationService.notifyAccountUpdated(saved);
        return accountMapper.toAccountResponse(saved);
    }

    @Override
    public BalanceResponse deposit(String username, MoneyAmountRequest moneyAmountRequest) {
        validateUsername(username);
        validateMoneyAmountRequest(moneyAmountRequest);
        log.info("Пополнение баланса: username={}, amount={}", username, moneyAmountRequest.getAmount());
        Account account = findRequiredAccount(username);
        BigDecimal amount = moneyAmountRequest.getAmount();
        account.setBalance(account.getBalance().add(amount));
        Account saved = accountRepository.save(account);
        log.info("Баланс пополнен: username={}, newBalance={}", username, saved.getBalance());
        accountNotificationService.notifyCashDeposit(username, amount);
        return accountMapper.toBalanceResponse(saved);
    }

    @Override
    public BalanceResponse withdraw(String username, MoneyAmountRequest moneyAmountRequest) {
        validateUsername(username);
        validateMoneyAmountRequest(moneyAmountRequest);
        log.info("Списание с баланса: username={}, amount={}", username, moneyAmountRequest.getAmount());
        Account account = findRequiredAccount(username);
        BigDecimal amount = moneyAmountRequest.getAmount();
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Недостаточно средств: username={}, balance={}, amount={}",
                    username, account.getBalance(), amount);
            throw new InsufficientFundsException(username);
        }
        account.setBalance(account.getBalance().subtract(amount));
        Account saved = accountRepository.save(account);
        log.info("Списание выполнено: username={}, newBalance={}", username, saved.getBalance());
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
        if (updateAccountRequest.getFullName().isBlank()) {
            throw new InvalidUpdateAccountRequestException("fullName must not be blank");
        }
        if (!isAdult(updateAccountRequest.getDateOfBirth())) {
            throw new InvalidUpdateAccountRequestException("User must be at least 18 years old");
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

    private boolean isAdult(LocalDate dateOfBirth) {
        return !dateOfBirth.isAfter(LocalDate.now().minusYears(MIN_AGE_YEARS));
    }
}
