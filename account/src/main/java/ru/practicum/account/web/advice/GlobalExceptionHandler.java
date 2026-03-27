package ru.practicum.account.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.practicum.account.domain.exception.AccountNotFoundException;
import ru.practicum.account.domain.exception.InsufficientFundsException;
import ru.practicum.account.domain.exception.InvalidAmountException;
import ru.practicum.account.domain.exception.InvalidMoneyAmountRequestException;
import ru.practicum.account.domain.exception.InvalidPaginationException;
import ru.practicum.account.domain.exception.InvalidUpdateAccountRequestException;
import ru.practicum.account.domain.exception.InvalidUsernameException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений Account-сервиса.
 *
 * <p>Любая ошибка приводится к единому формату ErrorResponse
 * с кодом, сообщением и timestamp.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Object> handleInsufficientFunds(InsufficientFundsException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "INSUFFICIENT_FUNDS", "Недостаточно средств на счёте", request);
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            OptimisticLockingFailureException.class
    })
    public ResponseEntity<Object> handleConflict(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "CONFLICT", "Concurrent update or data conflict", request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDatabaseError(DataAccessException ex, HttpServletRequest request) {
        log.error("Database access error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "DB_ERROR", "Database operation failed", request);
    }

    @ExceptionHandler({
            InvalidAmountException.class,
            InvalidMoneyAmountRequestException.class,
            InvalidPaginationException.class,
            InvalidUpdateAccountRequestException.class,
            InvalidUsernameException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<Object> handleBadRequest(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", extractValidationMessage(ex), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Insufficient permissions", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error", request);
    }

    private ResponseEntity<Object> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        if (isInternalApi(request)) {
            var body = new ru.practicum.account.domain.internalapi.ErrorResponse(code, message)
                    .timestamp(OffsetDateTime.now());
            return ResponseEntity.status(status).body(body);
        }
        var body = new ru.practicum.account.domain.publicapi.ErrorResponse(code, message)
                .timestamp(OffsetDateTime.now());
        return ResponseEntity.status(status).body(body);
    }

    private boolean isInternalApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/internal/");
    }

    private String extractValidationMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manve) {
            if (manve.getBindingResult().hasFieldErrors()) {
                return manve.getBindingResult().getFieldErrors().stream()
                        .map(this::formatFieldError)
                        .collect(Collectors.joining("; "));
            }
            return "Validation failed";
        }
        if (ex instanceof ConstraintViolationException cve) {
            return cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining("; "));
        }
        if (ex instanceof MethodArgumentTypeMismatchException matme) {
            return "Invalid parameter '" + matme.getName() + "'";
        }
        if (ex instanceof MissingServletRequestParameterException msrpe) {
            return "Missing required parameter '" + msrpe.getParameterName() + "'";
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "Malformed request body";
        }
        if (ex instanceof HandlerMethodValidationException hmve) {
            return hmve.getMessage();
        }
        return ex.getMessage() == null ? "Bad request" : ex.getMessage();
    }

    private String formatFieldError(FieldError fe) {
        String defaultMessage = fe.getDefaultMessage() == null ? "invalid value" : fe.getDefaultMessage();
        return fe.getField() + " " + defaultMessage;
    }
}
