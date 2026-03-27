package ru.practicum.transfer.service;

import ru.practicum.transfer.domain.TransferRequest;
import ru.practicum.transfer.domain.TransferResponse;

public interface TransferService {

    TransferResponse transfer(String usernameFrom, TransferRequest request);
}
