package com.hackathon.blockchain.dto;

import com.hackathon.blockchain.enums.SmartContractAction;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartContractRequestDTO {
    private String name; // Nombre del contrato
    private String conditionExpression; // Expresión de condición en SpEL
    private SmartContractAction action; // Acción a realizar si se cumple la condición
    private double actionValue; // Valor asociado a la acción
    private Long issuerWalletId; // ID de la wallet emisora
}
