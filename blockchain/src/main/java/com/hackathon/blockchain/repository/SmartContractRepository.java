package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.SmartContract;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmartContractRepository  extends JpaRepository<SmartContract ,Long> {
    Optional<SmartContract> findByName(@NotNull String symbol);
}
