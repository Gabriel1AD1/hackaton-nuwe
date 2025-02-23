package com.hackathon.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletBuyRequestDTO {

    @JsonProperty("symbol")
    @NotNull(message = "❌ The asset symbol cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9]{1,10}$", message = "❌ The asset symbol must be alphanumeric and between 1 and 10 characters")
    private String symbol;

    @JsonProperty("quantity")
    @NotNull(message = "❌ The quantity cannot be null")
    @Positive(message = "❌ The quantity must be greater than zero")
    private Double quantity;
}
