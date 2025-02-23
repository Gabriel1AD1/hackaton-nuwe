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
public class BlockResponseDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("blockIndex")
    private int blockIndex; // Número de bloque en la cadena

    @JsonProperty("timestamp")
    private long timestamp; // Fecha y hora de creación del bloque

    @JsonProperty("previousHash")
    private String previousHash; // Hash del bloque anterior

    @JsonProperty("nonce")
    private int nonce; // Número utilizado para la minería del bloque

    @JsonProperty("hash")
    private String hash; // Hash del bloque actual

    @JsonProperty("genesis")
    private boolean genesis; // Indica si es el bloque génesis
}
