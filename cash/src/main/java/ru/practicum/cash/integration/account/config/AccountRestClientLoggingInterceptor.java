package ru.practicum.cash.integration.account.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class AccountRestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final int MAX_BODY_LENGTH = 5000;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headersForLog = new HttpHeaders();
        headersForLog.putAll(request.getHeaders());
        if (headersForLog.containsHeader(HttpHeaders.AUTHORIZATION)) {
            headersForLog.set(HttpHeaders.AUTHORIZATION, "***");
        }

        String requestBody = formatBody(body);
        log.info(
                "Исходящий HTTP-запрос%n<<< Method: {}%n<<< URI: {}%n<<< Headers: {}%n<<< Body: {}",
                request.getMethod(),
                request.getURI(),
                headersForLog,
                requestBody
        );

        ClientHttpResponse response = execution.execute(request, body);
        log.info(
                "Исходящий HTTP-ответ%n>>> Method: {}%n>>> URI: {}%n>>> Status: {}",
                request.getMethod(),
                request.getURI(),
                response.getStatusCode()
        );
        return response;
    }

    private String formatBody(byte[] body) {
        if (body == null || body.length == 0) {
            return "<empty>";
        }
        String value = new String(body, StandardCharsets.UTF_8);
        if (value.length() > MAX_BODY_LENGTH) {
            return value.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
        }
        return value;
    }
}
