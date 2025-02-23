package com.hackathon.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletBalanceDTO {
    @JsonProperty("wallet_address")
    private String walletAddress;
    @JsonProperty("cash_balance")
    private double cashBalance;
    @JsonProperty("net_worth")
    private double netWorth;
    @JsonProperty("assets")
    private Map<String, Double> assets;
}
