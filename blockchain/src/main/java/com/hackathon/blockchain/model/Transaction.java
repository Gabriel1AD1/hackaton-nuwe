package com.hackathon.blockchain.model;

import com.hackathon.blockchain.enums.TransactionStatus;
import com.hackathon.blockchain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_transaction" )
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_wallet_id", nullable = false)
    private Wallet senderWallet;

    @ManyToOne
    @JoinColumn(name = "receiver_wallet_id", nullable = false)
    private Wallet receiverWallet;

    @Column(nullable = false)
    private String assetSymbol;

    @Column(nullable = false)
    private double quantity;

    @Column(nullable = false)
    private double pricePerUnit;
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private double fee;

    @Column
    private String block; // Para blockchain

    private BigDecimal amount;

}
