package ru.practicum.account.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.account.domain.model.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);

    @Query("""
            select a
            from Account a
            where a.username <> :currentUsername
              and (
                    :search is null
                    or lower(a.username) like lower(concat('%', :search, '%'))
                    or lower(a.fullName) like lower(concat('%', :search, '%'))
                  )
            """)
    Page<Account> findRecipients(@Param("currentUsername") String currentUsername,
                                 @Param("search") String search,
                                 Pageable pageable);
}
