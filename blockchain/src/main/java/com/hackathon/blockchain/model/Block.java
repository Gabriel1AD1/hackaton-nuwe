package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "block")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private int blockIndex; // Número de bloque en la cadena

    @Column(nullable = false, length = 64)
    private String hash; // Hash del bloque actual

    @Column(nullable = false, length = 64)
    private String previousHash; // Hash del bloque anterior

    @Column(nullable = false)
    private String data; // Información dentro del bloque

    @Column(nullable = false)
    private LocalDateTime timestamp; // Fecha y hora de creación del bloque

    private int nonce; // Número utilizado para la minería del bloque

    public String calculateHash() {
        String input = blockIndex + previousHash + data + timestamp + nonce;
        DigestUtils HashUtil = new DigestUtils();
        return HashUtil.sha256(input).toString();
    }
}
