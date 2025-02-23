package com.hackathon.blockchain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletSellRequestDTO {

    @NotBlank(message = "❌ The asset symbol cannot be empty")
    private String symbol;

    @Min(value = 0, message = "❌ The quantity must be greater than zero")
    private double quantity;
}
