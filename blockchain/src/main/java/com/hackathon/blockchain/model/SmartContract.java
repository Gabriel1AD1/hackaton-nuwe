package com.hackathon.blockchain.model;

import com.hackathon.blockchain.enums.SmartContractAction;
import com.hackathon.blockchain.enums.SmartContractStatus;
import com.hackathon.blockchain.enums.TransactionStatus;
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
    /**
     * Método para evaluar si la condición del contrato se cumple en una transacción.
     */
    public boolean evaluateCondition(Wallet wallet, Transaction transaction) {
        // Aquí podrías usar un motor de reglas como SpEL (Spring Expression Language)
        // O evaluar la condición con un sistema basado en scripts (Ejemplo: MVEL, JEXL)

        if (this.conditionExpression.equals("wallet.balance > 1000")) {
            return wallet.getBalance() > 1000;
        }

        // Más reglas pueden ser añadidas aquí...
        return false; // Por defecto, si no se reconoce la condición, retorna false.
    }

    /**
     * Método para ejecutar la acción del contrato si la condición se cumple.
     */
    public void executeAction(Wallet wallet, Transaction transaction) {
        if (this.action == SmartContractAction.CANCEL_TRANSACTION) {
            transaction.setStatus(TransactionStatus.CANCELED);
        } else if (this.action == SmartContractAction.TRANSFER_FEE && this.actionValue != null) {
            double fee = transaction.getAmount().doubleValue() * (this.actionValue / 100);
            wallet.setBalance(wallet.getBalance() - fee); // Se descuenta la tarifa
        }
    }
}
