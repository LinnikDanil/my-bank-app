package ru.practicum.account.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.account.domain.model.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.username = :username")
    Optional<Account> findByUsernameForUpdate(@Param("username") String username);

    Page<Account> findByUsernameNot(String currentUsername, Pageable pageable);

    @Query("""
            select a
            from Account a
            where a.username <> :currentUsername
              and (
                    lower(a.username) like lower(concat('%', :search, '%'))
                    or lower(a.fullName) like lower(concat('%', :search, '%'))
                  )
            """)
    Page<Account> findRecipientsBySearch(@Param("currentUsername") String currentUsername,
                                         @Param("search") String search,
                                         Pageable pageable);
}
