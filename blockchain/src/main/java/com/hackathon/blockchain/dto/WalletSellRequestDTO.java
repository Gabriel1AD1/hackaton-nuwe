package com.hackathon.blockchain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletSellRequestDTO {

    @NotBlank(message = "❌ The asset symbol cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9]{1,10}$", message = "❌ The asset symbol must be alphanumeric and between 1 and 10 characters")
    private String symbol;

    @Positive(message = "❌ The quantity must be greater than zero")
    private double quantity;
}
