package ru.practicum.cash.service;

import ru.practicum.cash.domain.CashOperationRequest;
import ru.practicum.cash.domain.CashOperationResponse;

public interface CashService {

    CashOperationResponse deposit(String username, CashOperationRequest request);

    CashOperationResponse withdraw(String username, CashOperationRequest request);
}
