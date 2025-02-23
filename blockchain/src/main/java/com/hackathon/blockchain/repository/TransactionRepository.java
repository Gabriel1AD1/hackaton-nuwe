package com.hackathon.blockchain.repository;

import com.hackathon.blockchain.enums.TransactionStatus;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findBySenderWallet(Wallet wallet);

    List<Transaction> findByReceiverWallet(Wallet wallet);

    List<Transaction> findByStatus(TransactionStatus pending);
}
