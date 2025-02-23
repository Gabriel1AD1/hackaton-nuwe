package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.*;
import com.hackathon.blockchain.model.Transaction;

public interface WalletService {

    String createWalletForUser(Long userId);

    WalletBalanceDTO  getWalletBalance(Long userId);

    // Método para transferir el fee: deducirlo del wallet del emisor y sumarlo a la wallet de fees.
    void transferFee(Transaction tx, double fee);


    WalletBuyResponseDTO walletBuy(WalletBuyRequestDTO dto, Long userId);

}