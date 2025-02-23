package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.enums.SmartContractAction;
import com.hackathon.blockchain.enums.TransactionStatus;
import com.hackathon.blockchain.model.SmartContract;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.repository.SmartContractRepository;
import com.hackathon.blockchain.repository.TransactionRepository;
import com.hackathon.blockchain.utils.SignatureUtil;
import lombok.AllArgsConstructor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PublicKey;
import java.util.List;

@Service
@AllArgsConstructor
public class SmartContractEvaluationService {

    private final SmartContractRepository smartContractRepository;
    private final TransactionRepository transactionRepository;
    private final WalletServiceI walletServiceI;
    private final WalletKeyServiceI walletKeyServiceI; // Para obtener la clave pública del emisor
    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * Verifica la firma digital del contrato usando la clave pública del emisor.
     */
    public boolean verifyContractSignature(SmartContract contract) {
        try {
            PublicKey issuerPublicKey = walletKeyServiceI.getPublicKeyForWallet(contract.getIssuerWalletId());
            if (issuerPublicKey == null) {
                return false;
            }
            String dataToSign = contract.getName() +
                                  contract.getConditionExpression() +
                                  contract.getAction() +
                                  contract.getActionValue() +
                                  contract.getIssuerWalletId();
            return SignatureUtil.verifySignature(dataToSign, contract.getDigitalSignature(), issuerPublicKey);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Evalúa todos los smart contracts activos sobre las transacciones pendientes.
     * Se inyectan las variables "amount" y "txType" en el contexto de SpEL.
     * Si la condición se cumple y la firma es válida, se ejecuta la acción definida:
     * - Para "CANCEL_TRANSACTION", se marca la transacción como "CANCELED".
     * - (Si hubiera otras acciones, se podrían implementar aquí).
     */
    @Transactional
    public void evaluateSmartContracts() {
        List<SmartContract> contracts = smartContractRepository.findAll(); // O filtrar por "ACTIVE"
        List<Transaction> pendingTxs = transactionRepository.findByStatus(TransactionStatus.PENDING);
        
        for (Transaction tx : pendingTxs) {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("amount", tx.getAmount());
            context.setVariable("txType", tx.getType());
            for (SmartContract contract : contracts) {
                if (!verifyContractSignature(contract)) continue;
                Expression exp = parser.parseExpression(contract.getConditionExpression());
                Boolean conditionMet = exp.getValue(context, Boolean.class);
                if (conditionMet != null && conditionMet) {
                    if (SmartContractAction.CANCEL_TRANSACTION.equals(contract.getAction())) {
                        tx.setStatus(TransactionStatus.CANCELED);
                    } else if (SmartContractAction.TRANSFER_FEE.equals(contract.getAction())) {
                        walletServiceI.transferFee(tx, contract.getActionValue());
                        tx.setStatus(TransactionStatus.PROCESSED_CONTRACT);
                    }
                    transactionRepository.save(tx);
                }
            }
        }
    }
}