package ru.practicum.cash.integration.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.cash.integration.account.api.AccountInternalApi;
import ru.practicum.cash.integration.account.client.ApiClient;
import ru.practicum.cash.integration.account.domain.MoneyAmountRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("AccountInternalApi")
class AccountInternalApiIT {

    @Test
    @DisplayName("internalDeposit")
    void test1() {
        var builder = ApiClient.buildRestClientBuilder();
        var server = MockRestServiceServer.bindTo(builder).build();

        var apiClient = new ApiClient(builder.build());
        apiClient.setBasePath("http://account");
        var api = new AccountInternalApi(apiClient);

        server.expect(requestTo("http://account/internal/v1/accounts/ivanivanov/deposit"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"username\":\"ivanivanov\",\"balance\":1250.50}", MediaType.APPLICATION_JSON));

        var response = api.internalDeposit("ivanivanov", new MoneyAmountRequest().amount(new BigDecimal("250.50")));

        assertThat(response.getUsername()).isEqualTo("ivanivanov");
        assertThat(response.getBalance()).isEqualByComparingTo("1250.50");
        server.verify();
    }
}
