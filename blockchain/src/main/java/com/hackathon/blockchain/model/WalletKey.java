package com.hackathon.blockchain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_wallet_keys")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "wallet_id", nullable = false, unique = true)
    private Wallet wallet;

    @Lob
    @Column(nullable = false)
    private String publicKey;

    @Lob
    @Column(nullable = false)
    private String privateKey;
}
