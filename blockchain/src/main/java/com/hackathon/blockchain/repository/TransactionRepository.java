package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.enums.TransactionStatus;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findBySenderWallet(Wallet wallet);

    List<Transaction> findByReceiverWallet(Wallet wallet);

    List<Transaction> findByStatus(TransactionStatus pending);
    @Query("SELECT t FROM Transaction t WHERE t.status = :status")
    List<Transaction> getPendingTransactions(@Param("status") TransactionStatus status);
    @Modifying
    @Query("UPDATE Transaction t SET t.status = :status WHERE t IN :pendingTransactions")
    void clearPendingTransactions(@Param("pendingTransactions") List<Transaction> pendingTransactions, @Param("status") TransactionStatus status);

}
