package com.hackathon.blockchain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {

    @JsonProperty("sent")
    private List<SentTransactionDTO> sent;

    @JsonProperty("received")
    private List<ReceivedTransactionDTO> received;

    @Setter
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SentTransactionDTO {
        private Integer id;
        private String assetSymbol;
        private Double amount;
        private Double pricePerUnit;
        private String type;
        private String timestamp;
        private String status;
        private Double fee;
        private Integer senderWalletId;
        private Integer receiverWalletId;
    }

    @Setter
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReceivedTransactionDTO {
        private Integer id;
        private String assetSymbol;
        private Double amount;
        private Double pricePerUnit;
        private String type;
        private String timestamp;
        private String status;
        private Double fee;
        private Integer senderWalletId;
        private Integer receiverWalletId;
    }
}
