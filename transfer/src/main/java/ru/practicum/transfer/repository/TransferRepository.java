package ru.practicum.transfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.transfer.domain.model.TransferEntity;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {
}
