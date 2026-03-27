package ru.practicum.front.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;
import ru.practicum.front.controller.dto.AccountDto;
import ru.practicum.front.controller.dto.CashAction;
import ru.practicum.front.integration.BankGatewayClient;
import ru.practicum.front.integration.account.domain.AccountResponse;
import ru.practicum.front.integration.account.domain.RecipientPageResponse;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final BankGatewayClient bankGatewayClient;

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
            bankGatewayClient.updateCurrentAccount(name, java.time.LocalDate.parse(birthdate));
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
        try {
            AccountResponse account = bankGatewayClient.getCurrentAccount();
            RecipientPageResponse recipientsPage = bankGatewayClient.getRecipients(recipientPage, recipientSize, recipientSearch);
            fillModel(model, account, recipientsPage, recipientSearch, selectedLogin, info, errors);
        } catch (RestClientResponseException ex) {
            log.warn("Ошибка загрузки данных главной страницы: status={}", ex.getStatusCode().value(), ex);
            model.addAttribute("errors", List.of(bankGatewayClient.extractErrorMessage(ex)));
            fillFallbackModel(model, recipientSearch, recipientPage, recipientSize);
        } catch (Exception ex) {
            log.error("Неожиданная ошибка загрузки главной страницы", ex);
            model.addAttribute("errors", List.of("Не удалось загрузить данные аккаунта"));
            fillFallbackModel(model, recipientSearch, recipientPage, recipientSize);
        }
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
        model.addAttribute("name", "");
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
