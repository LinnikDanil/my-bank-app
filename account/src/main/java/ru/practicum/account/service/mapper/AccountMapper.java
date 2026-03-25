package ru.practicum.account.service.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import ru.practicum.account.domain.model.Account;
import ru.practicum.account.domain.internalapi.BalanceResponse;
import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.publicapi.RecipientItem;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountResponse toAccountResponse(Account account);
    BalanceResponse toBalanceResponse(Account account);
    RecipientItem toRecipientItem(Account account);

    default RecipientPageResponse toRecipientPageResponse(Page<Account> accountPage) {
        List<RecipientItem> content = accountPage.getContent()
                .stream()
                .map(this::toRecipientItem)
                .toList();

        return new RecipientPageResponse(
                content,
                accountPage.getNumber(),
                accountPage.getSize(),
                accountPage.getTotalElements(),
                accountPage.getTotalPages(),
                accountPage.isLast()
        );
    }
}
