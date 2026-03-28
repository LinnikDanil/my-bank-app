package ru.practicum.transfer.service;

import ru.practicum.transfer.domain.TransferRequest;
import ru.practicum.transfer.domain.TransferResponse;

/**
 * Бизнес-контракт операций межпользовательского перевода средств.
 */
public interface TransferService {

    /**
     * Выполняет перевод средств от текущего пользователя к указанному получателю.
     *
     * @param usernameFrom username отправителя из JWT
     * @param request      параметры перевода
     * @return результат перевода
     */
    TransferResponse transfer(String usernameFrom, TransferRequest request);
}
