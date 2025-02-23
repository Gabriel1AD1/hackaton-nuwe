package com.hackathon.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletBuyResponseDTO {

    @JsonProperty("message")
    private String message;

    public static WalletBuyResponseDTO success() {
        return new WalletBuyResponseDTO("✅ Asset purchased successfully!");
    }

    public static WalletBuyResponseDTO error(String symbol) {
        return new WalletBuyResponseDTO("❌ Transaction blocked by smart contract conditions for " + symbol);
    }

    public static WalletBuyResponseDTO insufficientBalanceBuy(@NotNull String symbol) {
        return new WalletBuyResponseDTO("❌ Insufficient balance to buy "  + symbol);
    }
}
