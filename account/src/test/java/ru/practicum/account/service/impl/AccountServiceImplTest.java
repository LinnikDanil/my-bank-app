package ru.practicum.account.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.account.domain.exception.AccountNotFoundException;
import ru.practicum.account.domain.exception.InsufficientFundsException;
import ru.practicum.account.domain.exception.InvalidAmountException;
import ru.practicum.account.domain.exception.InvalidMoneyAmountRequestException;
import ru.practicum.account.domain.exception.InvalidPaginationException;
import ru.practicum.account.domain.exception.InvalidUpdateAccountRequestException;
import ru.practicum.account.domain.exception.InvalidUsernameException;
import ru.practicum.account.domain.model.Account;
import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;
import ru.practicum.account.integration.notification.service.AccountNotificationService;
import ru.practicum.account.repository.AccountRepository;
import ru.practicum.account.service.mapper.AccountMapper;
import ru.practicum.account.util.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountServiceImpl")
class AccountServiceImplTest {

    private static final String USERNAME = TestDataFactory.USERNAME;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountNotificationService accountNotificationService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Nested
    @DisplayName("getCurrentAccount")
    class GetCurrentAccount {

        @Test
        @DisplayName("ok")
        void getCurrentAccount_returnsMappedResponse_whenUserExists() {
            var account = TestDataFactory.createDefaultAccount();
            var expected = TestDataFactory.createAccountResponse(account);

            when(accountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(account));
            when(accountMapper.toAccountResponse(account)).thenReturn(expected);

            var result = accountService.getCurrentAccount(USERNAME);

            assertThat(result).isEqualTo(expected);
            verify(accountRepository, times(1)).findByUsername(USERNAME);
            verify(accountMapper, times(1)).toAccountResponse(account);
        }

        @Test
        @DisplayName("invalid username")
        void getCurrentAccount_throwsInvalidUsernameException_whenUsernameIsBlank() {
            assertThatExceptionOfType(InvalidUsernameException.class)
                    .isThrownBy(() -> accountService.getCurrentAccount(" "));

            verifyNoInteractions(accountRepository, accountMapper);
        }

        @Test
        @DisplayName("account not found")
        void getCurrentAccount_throwsAccountNotFoundException_whenUserDoesNotExist() {
            when(accountRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            assertThatExceptionOfType(AccountNotFoundException.class)
                    .isThrownBy(() -> accountService.getCurrentAccount(USERNAME));

            verify(accountMapper, never()).toAccountResponse(any());
        }
    }

    @Nested
    @DisplayName("getRecipients")
    class GetRecipients {

        @Test
        @DisplayName("ok and trims search")
        void getRecipients_trimsSearchAndReturnsMappedPage() {
            var recipients = List.of(
                    TestDataFactory.createAccount("petrpetrov", "Petr Petrov", LocalDate.of(1999, 11, 20), new BigDecimal("1000.00")),
                    TestDataFactory.createAccount("annaivanova", "Anna Ivanova", LocalDate.of(1998, 1, 1), new BigDecimal("500.00"))
            );
            var page = new PageImpl<>(recipients, PageRequest.of(0, 2), 2);
            var expected = new RecipientPageResponse(List.of(), 0, 2, 2L, 1, true);

            when(accountRepository.findRecipientsBySearch(USERNAME, "petr", PageRequest.of(0, 2))).thenReturn(page);
            when(accountMapper.toRecipientPageResponse(page)).thenReturn(expected);

            var result = accountService.getRecipients(USERNAME, 0, 2, "  petr  ");

            assertThat(result).isEqualTo(expected);
            verify(accountRepository, times(1)).findRecipientsBySearch(USERNAME, "petr", PageRequest.of(0, 2));
        }

        @Test
        @DisplayName("blank search becomes null")
        void getRecipients_usesFindByUsernameNot_whenSearchIsBlank() {
            var page = new PageImpl<Account>(List.of(), PageRequest.of(0, 20), 0);
            var expected = TestDataFactory.emptyRecipientPageResponse(0, 20);

            when(accountRepository.findByUsernameNot(USERNAME, PageRequest.of(0, 20))).thenReturn(page);
            when(accountMapper.toRecipientPageResponse(page)).thenReturn(expected);

            var result = accountService.getRecipients(USERNAME, 0, 20, "   ");

            assertThat(result).isEqualTo(expected);
            verify(accountRepository, times(1)).findByUsernameNot(USERNAME, PageRequest.of(0, 20));
        }

        @Test
        @DisplayName("invalid pagination")
        void getRecipients_throwsInvalidPaginationException_whenPagingParamsAreInvalid() {
            assertThatExceptionOfType(InvalidPaginationException.class)
                    .isThrownBy(() -> accountService.getRecipients(USERNAME, -1, 10, null));
            assertThatExceptionOfType(InvalidPaginationException.class)
                    .isThrownBy(() -> accountService.getRecipients(USERNAME, 0, 0, null));
            assertThatExceptionOfType(InvalidPaginationException.class)
                    .isThrownBy(() -> accountService.getRecipients(USERNAME, null, 10, null));

            verifyNoInteractions(accountRepository, accountMapper);
        }

        @Test
        @DisplayName("invalid username")
        void getRecipients_throwsInvalidUsernameException_whenUsernameIsInvalid() {
            assertThatExceptionOfType(InvalidUsernameException.class)
                    .isThrownBy(() -> accountService.getRecipients(null, 0, 10, null));

            verifyNoInteractions(accountRepository, accountMapper);
        }
    }

    @Nested
    @DisplayName("updateCurrentAccount")
    class UpdateCurrentAccount {

        @Test
        @DisplayName("ok")
        void updateCurrentAccount_returnsUpdatedAccount_whenRequestIsValid() {
            var account = TestDataFactory.createDefaultAccount();
            var request = TestDataFactory.createAdultUpdateRequest();
            var saved = TestDataFactory.createAccount(USERNAME, request.getFullName(), request.getDateOfBirth(), account.getBalance());
            var expected = new AccountResponse(USERNAME, request.getFullName(), request.getDateOfBirth(), account.getBalance());

            when(accountRepository.findByUsernameForUpdate(USERNAME)).thenReturn(Optional.of(account));
            when(accountRepository.save(account)).thenReturn(saved);
            when(accountMapper.toAccountResponse(saved)).thenReturn(expected);

            var result = accountService.updateCurrentAccount(USERNAME, request);

            assertThat(result).isEqualTo(expected);
            verify(accountRepository, times(1)).save(account);
            verify(accountNotificationService, times(1)).notifyAccountUpdated(saved);
        }

        @Test
        @DisplayName("request is null")
        void updateCurrentAccount_throwsInvalidUpdateAccountRequestException_whenRequestIsNull() {
            assertThatExceptionOfType(InvalidUpdateAccountRequestException.class)
                    .isThrownBy(() -> accountService.updateCurrentAccount(USERNAME, null));

            verifyNoInteractions(accountRepository, accountMapper, accountNotificationService);
        }

        @Test
        @DisplayName("full name is blank")
        void updateCurrentAccount_keepsCurrentFullName_whenProvidedFullNameIsBlank() {
            var account = TestDataFactory.createDefaultAccount();
            var request = new ru.practicum.account.domain.publicapi.UpdateAccountRequest()
                    .fullName("   ")
                    .dateOfBirth(LocalDate.now().minusYears(20));
            var saved = TestDataFactory.createAccount(
                    USERNAME,
                    account.getFullName(),
                    request.getDateOfBirth(),
                    account.getBalance()
            );
            var expected = new AccountResponse(USERNAME, account.getFullName(), request.getDateOfBirth(), account.getBalance());

            when(accountRepository.findByUsernameForUpdate(USERNAME)).thenReturn(Optional.of(account));
            when(accountRepository.save(account)).thenReturn(saved);
            when(accountMapper.toAccountResponse(saved)).thenReturn(expected);

            var result = accountService.updateCurrentAccount(USERNAME, request);

            assertThat(result).isEqualTo(expected);
            verify(accountRepository, times(1)).save(account);
            verify(accountNotificationService, times(1)).notifyAccountUpdated(saved);
        }

        @Test
        @DisplayName("user is younger than 18")
        void updateCurrentAccount_throwsInvalidUpdateAccountRequestException_whenUserIsMinor() {
            var request = TestDataFactory.createMinorUpdateRequest();

            assertThatExceptionOfType(InvalidUpdateAccountRequestException.class)
                    .isThrownBy(() -> accountService.updateCurrentAccount(USERNAME, request));

            verifyNoInteractions(accountRepository, accountMapper, accountNotificationService);
        }

        @Test
        @DisplayName("account not found")
        void updateCurrentAccount_throwsAccountNotFoundException_whenAccountIsMissing() {
            when(accountRepository.findByUsernameForUpdate(USERNAME)).thenReturn(Optional.empty());

            assertThatExceptionOfType(AccountNotFoundException.class)
                    .isThrownBy(() -> accountService.updateCurrentAccount(USERNAME, TestDataFactory.createAdultUpdateRequest()));

            verify(accountRepository, never()).save(any());
            verifyNoInteractions(accountNotificationService);
        }
    }

    @Nested
    @DisplayName("deposit")
    class Deposit {

        @Test
        @DisplayName("ok")
        void deposit_returnsUpdatedBalance_whenRequestIsValid() {
            var account = TestDataFactory.createDefaultAccount();
            var request = TestDataFactory.createMoneyAmountRequest("250.50");
            var saved = TestDataFactory.createAccount(USERNAME, account.getFullName(), account.getDateOfBirth(), new BigDecimal("1250.50"));
            var expected = new ru.practicum.account.domain.internalapi.BalanceResponse(USERNAME, new BigDecimal("1250.50"));

            when(accountRepository.findByUsernameForUpdate(USERNAME)).thenReturn(Optional.of(account));
            when(accountRepository.save(account)).thenReturn(saved);
            when(accountMapper.toBalanceResponse(saved)).thenReturn(expected);

            var result = accountService.deposit(USERNAME, request);

            assertThat(result).isEqualTo(expected);
            verify(accountNotificationService, times(1)).notifyCashDeposit(USERNAME, new BigDecimal("250.50"));
        }

        @Test
        @DisplayName("request is null")
        void deposit_throwsInvalidMoneyAmountRequestException_whenRequestIsNull() {
            assertThatExceptionOfType(InvalidMoneyAmountRequestException.class)
                    .isThrownBy(() -> accountService.deposit(USERNAME, null));

            verifyNoInteractions(accountRepository, accountMapper, accountNotificationService);
        }

        @Test
        @DisplayName("amount must be positive")
        void deposit_throwsInvalidAmountException_whenAmountIsNotPositive() {
            assertThatExceptionOfType(InvalidAmountException.class)
                    .isThrownBy(() -> accountService.deposit(USERNAME, new ru.practicum.account.domain.internalapi.MoneyAmountRequest(BigDecimal.ZERO)));
            assertThatExceptionOfType(InvalidAmountException.class)
                    .isThrownBy(() -> accountService.deposit(USERNAME, new ru.practicum.account.domain.internalapi.MoneyAmountRequest(new BigDecimal("-1.00"))));

            verifyNoInteractions(accountRepository, accountMapper, accountNotificationService);
        }

        @Test
        @DisplayName("account not found")
        void deposit_throwsAccountNotFoundException_whenAccountIsMissing() {
            when(accountRepository.findByUsernameForUpdate(USERNAME)).thenReturn(Optional.empty());

            assertThatExceptionOfType(AccountNotFoundException.class)
                    .isThrownBy(() -> accountService.deposit(USERNAME, TestDataFactory.createMoneyAmountRequest("10.00")));

            verify(accountRepository, never()).save(any());
            verifyNoInteractions(accountNotificationService);
        }
    }

    @Nested
    @DisplayName("withdraw")
    class Withdraw {

        @Test
        @DisplayName("ok")
        void withdraw_returnsUpdatedBalance_whenRequestIsValid() {
            var account = TestDataFactory.createDefaultAccount();
            var request = TestDataFactory.createMoneyAmountRequest("125.00");
            var saved = TestDataFactory.createAccount(USERNAME, account.getFullName(), account.getDateOfBirth(), new BigDecimal("875.00"));
            var expected = new ru.practicum.account.domain.internalapi.BalanceResponse(USERNAME, new BigDecimal("875.00"));

            when(accountRepository.findByUsernameForUpdate(USERNAME)).thenReturn(Optional.of(account));
            when(accountRepository.save(account)).thenReturn(saved);
            when(accountMapper.toBalanceResponse(saved)).thenReturn(expected);

            var result = accountService.withdraw(USERNAME, request);

            assertThat(result).isEqualTo(expected);
            verify(accountNotificationService, times(1)).notifyCashWithdraw(USERNAME, new BigDecimal("125.00"));
        }

        @Test
        @DisplayName("insufficient funds")
        void withdraw_throwsInsufficientFundsException_whenBalanceIsNotEnough() {
            var account = TestDataFactory.createDefaultAccount();
            when(accountRepository.findByUsernameForUpdate(USERNAME)).thenReturn(Optional.of(account));

            assertThatExceptionOfType(InsufficientFundsException.class)
                    .isThrownBy(() -> accountService.withdraw(USERNAME, TestDataFactory.createMoneyAmountRequest("5000.00")));

            verify(accountRepository, never()).save(any());
            verifyNoInteractions(accountNotificationService);
        }

        @Test
        @DisplayName("amount must be positive")
        void withdraw_throwsInvalidAmountException_whenAmountIsNotPositive() {
            assertThatExceptionOfType(InvalidAmountException.class)
                    .isThrownBy(() -> accountService.withdraw(USERNAME, new ru.practicum.account.domain.internalapi.MoneyAmountRequest(BigDecimal.ZERO)));

            verifyNoInteractions(accountRepository, accountMapper, accountNotificationService);
        }

        @Test
        @DisplayName("account not found")
        void withdraw_throwsAccountNotFoundException_whenAccountIsMissing() {
            when(accountRepository.findByUsernameForUpdate(USERNAME)).thenReturn(Optional.empty());

            assertThatExceptionOfType(AccountNotFoundException.class)
                    .isThrownBy(() -> accountService.withdraw(USERNAME, TestDataFactory.createMoneyAmountRequest("10.00")));

            verify(accountRepository, never()).save(any());
            verifyNoInteractions(accountNotificationService);
        }
    }
}
