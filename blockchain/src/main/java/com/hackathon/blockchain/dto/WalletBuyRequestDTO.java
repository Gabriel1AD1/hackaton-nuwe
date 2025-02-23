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
public class WalletBuyRequestDTO {

    @JsonProperty("symbol")
    @NotNull
    private String symbol;

    @JsonProperty("quantity")
    @NotNull
    private Double quantity;
}
