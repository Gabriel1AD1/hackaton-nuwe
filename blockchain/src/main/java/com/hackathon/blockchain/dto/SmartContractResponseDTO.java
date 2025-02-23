package com.hackathon.blockchain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartContractResponseDTO
{
    private Long id; // ID del contrato inteligente
    private String name; // Nombre del contrato
    private String conditionExpression; // Expresión de condición en SpEL
    private String action; // Acción a realizar si se cumple la condición
    private double actionValue; // Valor asociado a la acción
    private Long issuerWalletId; // ID de la wallet emisora
    private String digitalSignature; // Firma digital del contrato
}
