package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.TransactionDTO;

public interface TransactionService {
    TransactionDTO getTransactionForUserId(Long userId);
}
