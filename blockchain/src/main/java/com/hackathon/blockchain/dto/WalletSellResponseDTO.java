package com.hackathon.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletSellResponseDTO {

    private String message;

    public static WalletSellResponseDTO success() {
        return WalletSellResponseDTO.builder()
                .message("✅ Asset sold successfully!")
                .build();
    }

    public static WalletSellResponseDTO error(String symbol) {
        return WalletSellResponseDTO.builder()
                .message("❌ Transaction blocked by smart contract conditions for " + symbol)
                .build();
    }

    public static WalletSellResponseDTO insufficientAssets(String symbol) {
        return WalletSellResponseDTO.builder()
                .message("❌ Insufficient " + symbol + " to sell")
                .build();
    }
}