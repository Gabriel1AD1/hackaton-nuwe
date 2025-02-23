package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet,Long> {
    @Query("select w from Wallet w where w.user.id = ?1")
    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByAddress(String address);
}
