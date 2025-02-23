package com.hackathon.blockchain.model;

import com.hackathon.blockchain.enums.SmartContractAction;
import com.hackathon.blockchain.enums.SmartContractStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_smart_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 500)
    private String conditionExpression; // Expresión en SpEL

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SmartContractAction action; // Ej: "CANCEL_TRANSACTION", "TRANSFER_FEE"

    @Column(nullable = true)
    private Double actionValue;

    @Column(nullable = false)
    private Long issuerWalletId; // Billetera del creador del contrato

    @Column(nullable = false, length = 500)
    private String digitalSignature; // Firma para validación

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SmartContractStatus status;

}
