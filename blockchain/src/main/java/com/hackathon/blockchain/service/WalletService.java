package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.*;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WalletService {
    Optional<Wallet> getWalletByUserId(Long userId);
    Optional<Wallet> getWalletByAddress(String address);
    void initializeLiquidityPools(Map<String, Double> initialAssets);

    /*
     * Esta versión ya no almacena purchasePrice en Assets
     */
    void updateWalletAssets(Wallet wallet, String assetSymbol, double amount);

    String createWalletForUser(Long userId);

    WalletBalanceDTO  getWalletBalance(Long userId);

    Map<String, List<Transaction>> getWalletTransactions(Long walletId);

    // Método para transferir el fee: deducirlo del wallet del emisor y sumarlo a la wallet de fees.
    void transferFee(Transaction tx, double fee);

    // Método para crear una wallet para fees (solo USDT)
    String createFeeWallet();

    Wallet findByUser(String username);


    Map<String, Double> fetchLiveMarketPrices();

    ResponseDTO fetchLivePriceForAsset(String symbol);


    WalletBuyResponseDTO walletBuy(WalletBuyRequestDTO dto, Long userId);

    WalletSellResponseDTO walletSell(WalletSellRequestDTO dto, Long userId);
}