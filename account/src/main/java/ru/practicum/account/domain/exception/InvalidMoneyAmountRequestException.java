package ru.practicum.account.domain.exception;

public class InvalidMoneyAmountRequestException extends AccountException {
    public InvalidMoneyAmountRequestException() {
        super("Money amount request is required");
    }
}
