package com.hackathon.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletGenerateKeysDTO {
    @JsonProperty("message")
    private String message;
    @JsonProperty("publicKey")
    private String publicKey;
    @JsonProperty("absolutePath")
    private String absolutePath;
    public static WalletGenerateKeysDTO generateKey(String publicKey, String absolutePath, Long generate) {
        String pemPublicKey = "-----BEGIN PUBLIC KEY-----\n" +
                publicKey +
                "\n-----END PUBLIC KEY-----";

        return WalletGenerateKeysDTO.builder()
                .message("Keys generated/retrieved successfully for wallet id: " + generate)
                .publicKey(pemPublicKey)
                .absolutePath(absolutePath)
                .build();
    }

}
