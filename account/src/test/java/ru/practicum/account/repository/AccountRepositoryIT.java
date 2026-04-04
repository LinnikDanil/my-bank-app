package ru.practicum.account.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.account.util.PostgresContainer;
import ru.practicum.account.util.TestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ImportTestcontainers(PostgresContainer.class)
@ActiveProfiles("test")
@DisplayName("AccountRepository")
class AccountRepositoryIT {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.execute("TRUNCATE TABLE account RESTART IDENTITY CASCADE");
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsername {

        @Test
        @DisplayName("returns account")
        void returnsAccount() {
            var account = TestDataFactory.createDefaultAccount();
            accountRepository.save(account);

            var result = accountRepository.findByUsername(account.getUsername());

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo(account.getUsername());
            assertThat(result.get().getFullName()).isEqualTo(account.getFullName());
            assertThat(result.get().getBalance()).isEqualByComparingTo(account.getBalance());
        }

        @Test
        @DisplayName("returns empty when not found")
        void returnsEmptyWhenNotFound() {
            var result = accountRepository.findByUsername("missing-user");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findRecipients")
    class FindRecipients {

        @BeforeEach
        void seed() {
            accountRepository.save(TestDataFactory.createAccount(
                    "ivanivanov", "Ivan Ivanov", LocalDate.of(2001, 5, 10), new BigDecimal("1000.00")
            ));
            accountRepository.save(TestDataFactory.createAccount(
                    "petrpetrov", "Petr Petrov", LocalDate.of(1999, 11, 20), new BigDecimal("800.00")
            ));
            accountRepository.save(TestDataFactory.createAccount(
                    "petya1995", "Petya Smirnov", LocalDate.of(1995, 4, 4), new BigDecimal("600.00")
            ));
            accountRepository.save(TestDataFactory.createAccount(
                    "annaivanova", "Anna Ivanova", LocalDate.of(1998, 1, 1), new BigDecimal("500.00")
            ));
        }

        @Test
        @DisplayName("excludes current user and filters by search")
        void excludesCurrentUserAndFiltersBySearch() {
            var result = accountRepository.findRecipientsBySearch(
                    "ivanivanov",
                    "pet",
                    PageRequest.of(0, 10)
            );

            assertThat(result.getContent())
                    .extracting(a -> a.getUsername())
                    .containsExactlyInAnyOrder("petrpetrov", "petya1995");
            assertThat(result.getContent())
                    .extracting(a -> a.getUsername())
                    .doesNotContain("ivanivanov");
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns all except current user when search is null")
        void returnsAllExceptCurrentUserWhenSearchIsNull() {
            var result = accountRepository.findByUsernameNot("ivanivanov", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .extracting(a -> a.getUsername())
                    .containsExactlyInAnyOrder("petrpetrov", "petya1995", "annaivanova");
            assertThat(result.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("search is case insensitive")
        void searchIsCaseInsensitive() {
            var result = accountRepository.findRecipientsBySearch(
                    "ivanivanov",
                    "PETR",
                    PageRequest.of(0, 10)
            );

            assertThat(result.getContent())
                    .extracting(a -> a.getUsername())
                    .containsExactly("petrpetrov");
        }

        @Test
        @DisplayName("applies pagination")
        void appliesPagination() {
            var result = accountRepository.findByUsernameNot("ivanivanov", PageRequest.of(0, 2));

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("returns empty when no matches")
        void returnsEmptyWhenNoMatches() {
            var result = accountRepository.findRecipientsBySearch(
                    "ivanivanov",
                    "zzzz-no-match",
                    PageRequest.of(0, 10)
            );

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
        }
    }
}
