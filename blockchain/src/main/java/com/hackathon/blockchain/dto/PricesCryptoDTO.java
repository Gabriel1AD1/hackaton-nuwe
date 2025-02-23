package com.hackathon.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class PricesCryptoDTO {
    private String nameCrypto;
    @JsonProperty("valueCry")
    private BigDecimal valueCrypto;
}
