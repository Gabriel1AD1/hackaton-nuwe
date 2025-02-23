package com.hackathon.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    @JsonProperty("message")
    private String message;
    public static ResponseDTO registerMessage() {
        return new ResponseDTOBuilder().message("User registered and logged in successfully").build();
    }
    public static ResponseDTO checkAuthMessage(String username) {
        return new ResponseDTOBuilder().message("Check user successful hello : ".concat(username)).build();
    }

    public static ResponseDTO loginSuccessful() {
        return new ResponseDTOBuilder().message("Login successful").build();
    }

    public static ResponseDTO logoutMessage() {
        return  new ResponseDTOBuilder().message("Logged out successfully").build();
    }

    public static ResponseDTO createWallerMessage(String message) {
        return new ResponseDTOBuilder().message(message).build();
    }
    public static ResponseDTO createMessageForPriceAsset(String symbol, Double price) {
        return new ResponseDTOBuilder().message("Current price of " + symbol.toUpperCase() + ": $" + price).build();
    }

    public static ResponseDTO simulateMiningMessage(String blockHash) {
        return new ResponseDTOBuilder().message("Block mined: " + blockHash).build();
    }

    public static ResponseDTO noPendingTransactionMine() {
        return new ResponseDTOBuilder().message("❌ No pending transactions to mine.").build();
    }
}
