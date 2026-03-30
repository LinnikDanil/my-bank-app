package ru.practicum.front.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.front.controller.dto.AccountDto;
import ru.practicum.front.controller.dto.CashAction;
import ru.practicum.front.integration.BankApiClient;
import ru.practicum.front.integration.account.domain.AccountResponse;
import ru.practicum.front.integration.account.domain.RecipientPageResponse;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final BankApiClient bankGatewayClient;

    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String getAccount(
            Model model,
            @RequestParam(value = "recipientSearch", required = false) String recipientSearch,
            @RequestParam(value = "recipientPage", required = false) Integer recipientPage,
            @RequestParam(value = "recipientSize", required = false) Integer recipientSize,
            @RequestParam(value = "selectedLogin", required = false) String selectedLogin
    ) {
        renderMain(model, sanitizeSearch(recipientSearch), normalizePage(recipientPage), normalizeSize(recipientSize),
                selectedLogin, null, null);
        return "main";
    }

    @PostMapping("/account")
    public String editAccount(
            Model model,
            @RequestParam("name") String name,
            @RequestParam("birthdate") String birthdate,
            @RequestParam(value = "recipientSearch", required = false) String recipientSearch,
            @RequestParam(value = "recipientPage", required = false) Integer recipientPage,
            @RequestParam(value = "recipientSize", required = false) Integer recipientSize,
            @RequestParam(value = "selectedLogin", required = false) String selectedLogin
    ) {
        String info = null;
        List<String> errors = null;
        try {
            String resolvedName = resolveNameForUpdate(name);
            log.info("Обновление аккаунта: submittedNameBlank={}, resolvedName='{}', birthdate='{}'",
                    name == null || name.isBlank(), resolvedName, birthdate);
            bankGatewayClient.updateCurrentAccount(resolvedName, java.time.LocalDate.parse(birthdate));
            info = "Данные аккаунта сохранены";
        } catch (RestClientResponseException ex) {
            log.warn("Ошибка обновления аккаунта: status={}", ex.getStatusCode().value(), ex);
            errors = List.of(bankGatewayClient.extractErrorMessage(ex));
        } catch (Exception ex) {
            log.error("Неожиданная ошибка обновления аккаунта", ex);
            errors = List.of("Не удалось обновить данные аккаунта");
        }

        renderMain(model, sanitizeSearch(recipientSearch), normalizePage(recipientPage), normalizeSize(recipientSize),
                selectedLogin, info, errors);
        return "main";
    }

    @PostMapping("/cash")
    public String editCash(
            Model model,
            @RequestParam("value") BigDecimal value,
            @RequestParam("action") CashAction action,
            @RequestParam(value = "recipientSearch", required = false) String recipientSearch,
            @RequestParam(value = "recipientPage", required = false) Integer recipientPage,
            @RequestParam(value = "recipientSize", required = false) Integer recipientSize,
            @RequestParam(value = "selectedLogin", required = false) String selectedLogin
    ) {
        String info = null;
        List<String> errors = null;
        try {
            if (action == CashAction.GET) {
                bankGatewayClient.withdraw(value);
                info = "Снято " + value.stripTrailingZeros().toPlainString() + " руб.";
            } else {
                bankGatewayClient.deposit(value);
                info = "Положено " + value.stripTrailingZeros().toPlainString() + " руб.";
            }
        } catch (RestClientResponseException ex) {
            log.warn("Ошибка операции с наличными: status={}", ex.getStatusCode().value(), ex);
            errors = List.of(bankGatewayClient.extractErrorMessage(ex));
        } catch (Exception ex) {
            log.error("Неожиданная ошибка операции с наличными", ex);
            errors = List.of("Не удалось выполнить операцию с наличными");
        }

        renderMain(model, sanitizeSearch(recipientSearch), normalizePage(recipientPage), normalizeSize(recipientSize),
                selectedLogin, info, errors);
        return "main";
    }

    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @RequestParam("value") BigDecimal value,
            @RequestParam("login") String login,
            @RequestParam(value = "recipientSearch", required = false) String recipientSearch,
            @RequestParam(value = "recipientPage", required = false) Integer recipientPage,
            @RequestParam(value = "recipientSize", required = false) Integer recipientSize
    ) {
        String info = null;
        List<String> errors = null;
        try {
            bankGatewayClient.transfer(login, value);
            info = "Успешно переведено " + value.stripTrailingZeros().toPlainString() + " руб.";
        } catch (RestClientResponseException ex) {
            log.warn("Ошибка перевода: status={}", ex.getStatusCode().value(), ex);
            errors = List.of(bankGatewayClient.extractErrorMessage(ex));
        } catch (Exception ex) {
            log.error("Неожиданная ошибка перевода", ex);
            errors = List.of("Не удалось выполнить перевод");
        }

        renderMain(model, sanitizeSearch(recipientSearch), normalizePage(recipientPage), normalizeSize(recipientSize),
                login, info, errors);
        return "main";
    }

    private void renderMain(
            Model model,
            String recipientSearch,
            int recipientPage,
            int recipientSize,
            String selectedLogin,
            String info,
            List<String> errors
    ) {
        List<String> allErrors = errors == null ? new ArrayList<>() : new ArrayList<>(errors);
        AccountResponse account = null;
        RecipientPageResponse recipientsPage = emptyRecipientPage(recipientPage, recipientSize);

        try {
            account = bankGatewayClient.getCurrentAccount();
        } catch (RestClientResponseException ex) {
            log.warn("Ошибка загрузки данных аккаунта: status={}", ex.getStatusCode().value(), ex);
            allErrors.add(bankGatewayClient.extractErrorMessage(ex));
        } catch (Exception ex) {
            log.error("Неожиданная ошибка загрузки данных аккаунта", ex);
            allErrors.add("Не удалось загрузить данные аккаунта");
        }

        try {
            recipientsPage = bankGatewayClient.getRecipients(recipientPage, recipientSize, recipientSearch);
        } catch (RestClientResponseException ex) {
            log.warn("Ошибка загрузки списка получателей: status={}", ex.getStatusCode().value(), ex);
            allErrors.add(bankGatewayClient.extractErrorMessage(ex));
        } catch (Exception ex) {
            log.error("Неожиданная ошибка загрузки списка получателей", ex);
            allErrors.add("Не удалось загрузить список получателей");
        }

        List<String> resolvedErrors = allErrors.isEmpty() ? null : allErrors;
        if (account == null) {
            fillFallbackModel(model, recipientSearch, recipientPage, recipientSize);
            model.addAttribute("errors", resolvedErrors);
            model.addAttribute("info", info);
            return;
        }
        fillModel(model, account, recipientsPage, recipientSearch, selectedLogin, info, resolvedErrors);
    }

    private RecipientPageResponse emptyRecipientPage(int recipientPage, int recipientSize) {
        return new RecipientPageResponse()
                .content(Collections.emptyList())
                .page(recipientPage)
                .size(recipientSize)
                .totalElements(0L)
                .totalPages(0)
                .last(true);
    }

    private void fillModel(
            Model model,
            AccountResponse account,
            RecipientPageResponse recipientsPage,
            String recipientSearch,
            String selectedLogin,
            String info,
            List<String> errors
    ) {
        List<AccountDto> accounts = recipientsPage.getContent().stream()
                .map(item -> new AccountDto(item.getUsername(), item.getFullName()))
                .toList();

        String resolvedSelectedLogin = selectedLogin;
        if ((resolvedSelectedLogin == null || resolvedSelectedLogin.isBlank()) && !accounts.isEmpty()) {
            resolvedSelectedLogin = accounts.getFirst().login();
        }

        model.addAttribute("name", account.getFullName());
        model.addAttribute("birthdate", account.getDateOfBirth().format(DATE_FORMATTER));
        model.addAttribute("sum", account.getBalance().stripTrailingZeros().toPlainString());
        model.addAttribute("accounts", accounts);
        model.addAttribute("recipientSearch", recipientSearch);
        model.addAttribute("recipientPage", recipientsPage.getPage());
        model.addAttribute("recipientSize", recipientsPage.getSize());
        model.addAttribute("recipientTotalPages", recipientsPage.getTotalPages());
        model.addAttribute("recipientLast", recipientsPage.getLast());
        model.addAttribute("selectedLogin", resolvedSelectedLogin);
        model.addAttribute("errors", errors);
        model.addAttribute("info", info);
    }

    private void fillFallbackModel(Model model, String recipientSearch, int recipientPage, int recipientSize) {
        model.addAttribute("name", resolveDefaultName());
        model.addAttribute("birthdate", "");
        model.addAttribute("sum", "0");
        model.addAttribute("accounts", Collections.emptyList());
        model.addAttribute("recipientSearch", recipientSearch);
        model.addAttribute("recipientPage", recipientPage);
        model.addAttribute("recipientSize", recipientSize);
        model.addAttribute("recipientTotalPages", 0);
        model.addAttribute("recipientLast", true);
        model.addAttribute("selectedLogin", "");
        if (!model.containsAttribute("info")) {
            model.addAttribute("info", null);
        }
    }

    private String resolveDefaultName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OidcUser oidcUser)) {
            return "";
        }

        String fullName = oidcUser.getFullName();
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }

        String givenName = oidcUser.getGivenName();
        String familyName = oidcUser.getFamilyName();
        if (givenName != null && !givenName.isBlank() && familyName != null && !familyName.isBlank()) {
            return familyName + " " + givenName;
        }

        String preferredUsername = oidcUser.getPreferredUsername();
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }
        return "";
    }

    private String resolveNameForUpdate(String submittedName) {
        if (submittedName != null) {
            String trimmedSubmittedName = submittedName.trim();
            if (!trimmedSubmittedName.isBlank()) {
                return trimmedSubmittedName;
            }
        }
        try {
            AccountResponse account = bankGatewayClient.getCurrentAccount();
            String trimmedAccountName = account.getFullName().trim();
            if (!trimmedAccountName.isBlank()) {
                return trimmedAccountName;
            }
        } catch (Exception _) {
        }
        String identityName = resolveDefaultName();
        if (!identityName.isBlank()) {
            return identityName.trim();
        }
        return "Пользователь";
    }

    private int normalizePage(Integer recipientPage) {
        if (recipientPage == null || recipientPage < 0) {
            return DEFAULT_PAGE;
        }
        return recipientPage;
    }

    private int normalizeSize(Integer recipientSize) {
        if (recipientSize == null || recipientSize < 1 || recipientSize > 100) {
            return DEFAULT_SIZE;
        }
        return recipientSize;
    }

    private String sanitizeSearch(String search) {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
