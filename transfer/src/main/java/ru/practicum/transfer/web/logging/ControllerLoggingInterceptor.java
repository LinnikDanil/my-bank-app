package ru.practicum.transfer.web.logging;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerLoggingInterceptor implements HandlerInterceptor {

    private static final int MAX_BODY_LENGTH = 5000;

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            Map<String, List<String>> headers = extractHeaders(request);
            String body = extractBody(request);

            String loggedBody = prepareBodyForLogging(body);
            String formattedHeaders = toJsonSafe(headers);

            String message = String.format(
                    "Запрос к методу контроллера %s%n<<< Type: HTTP%n<<< Method: %s%n<<< Headers: %s%n<<< Body: %s",
                    request.getRequestURI(),
                    request.getMethod(),
                    formattedHeaders,
                    loggedBody
            );
            log.info(message);
        } catch (Exception e) {
            log.debug("Ошибка логирования HTTP-запроса: {}", e.getMessage(), e);
        }
        return true;
    }

    private Map<String, List<String>> extractHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, Collections.list(request.getHeaders(headerName)));
        }
        return headers;
    }

    private String extractBody(HttpServletRequest request) {
        if (request instanceof CachedBodyHttpServletRequest cached) {
            return new String(cached.getCachedBody(), StandardCharsets.UTF_8);
        }
        return "";
    }

    private String prepareBodyForLogging(String body) {
        if (body == null || body.isBlank()) {
            return "<empty>";
        }
        String trimmed = body.trim();
        String pretty = tryPrettyJson(trimmed);
        if (pretty.length() > MAX_BODY_LENGTH) {
            return pretty.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
        }
        return pretty;
    }

    private String tryPrettyJson(String body) {
        try {
            Object parsed = objectMapper.readValue(body, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
        } catch (Exception ignored) {
            return body;
        }
    }

    private String toJsonSafe(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
