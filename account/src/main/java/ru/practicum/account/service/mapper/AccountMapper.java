package ru.practicum.account.service.mapper;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import ru.practicum.account.domain.model.Account;
import ru.practicum.account.domain.internalapi.BalanceResponse;
import ru.practicum.account.domain.publicapi.AccountResponse;
import ru.practicum.account.domain.publicapi.RecipientItem;
import ru.practicum.account.domain.publicapi.RecipientPageResponse;

import java.util.List;

/**
 * MapStruct-маппер для преобразования доменной модели {@link Account}
 * в DTO публичного и внутреннего API.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {
    /**
     * Преобразует сущность аккаунта в публичный ответ.
     */
    AccountResponse toAccountResponse(Account account);

    /**
     * Преобразует сущность аккаунта в ответ с балансом.
     */
    BalanceResponse toBalanceResponse(Account account);

    /**
     * Преобразует сущность аккаунта в элемент списка получателей.
     */
    RecipientItem toRecipientItem(Account account);

    /**
     * Собирает страницу получателей из страницы сущностей.
     * Поля пагинации сохраняются из исходного {@link Page}.
     */
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
