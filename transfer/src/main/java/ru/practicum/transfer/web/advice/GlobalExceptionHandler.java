package ru.practicum.transfer.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.practicum.transfer.domain.exception.AccountNotFoundException;
import ru.practicum.transfer.domain.exception.InsufficientFundsException;
import ru.practicum.transfer.domain.exception.InvalidAmountException;
import ru.practicum.transfer.domain.exception.InvalidTransferRequestException;
import ru.practicum.transfer.domain.exception.InvalidUsernameException;
import ru.practicum.transfer.domain.exception.UpstreamServiceException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Object> handleInsufficientFunds(InsufficientFundsException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "INSUFFICIENT_FUNDS", ex.getMessage());
    }

    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<Object> handleUpstreamError(UpstreamServiceException ex, HttpServletRequest request) {
        log.error("Upstream service error", ex);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", ex.getMessage());
    }

    @ExceptionHandler({
            InvalidAmountException.class,
            InvalidTransferRequestException.class,
            InvalidUsernameException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<Object> handleBadRequest(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", extractValidationMessage(ex));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Insufficient permissions");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal server error");
    }

    private ResponseEntity<Object> error(HttpStatus status, String code, String message) {
        var body = new ru.practicum.transfer.domain.ErrorResponse(code, message)
                .timestamp(OffsetDateTime.now());
        return ResponseEntity.status(status).body(body);
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
