package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.dto.TransactionDTO;
import com.hackathon.blockchain.exception.EntityNotFoundException;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.TransactionRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TransactionServiceI implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    @Override
    public TransactionDTO getTransactionForUserId(Long userId)  {
        // Obtener las wallets del usuario
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);
        if (optionalWallet.isEmpty()) {
            throw new EntityNotFoundException("Wallet not found for user");
        }

        Wallet wallet = optionalWallet.get();

        // Obtener las transacciones enviadas y recibidas
        List<Transaction> sentTransactions = transactionRepository.findBySenderWallet(wallet);
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverWallet(wallet);

        // Convertir las transacciones enviadas a DTO
        List<TransactionDTO.SentTransactionDTO> sentTransactionDTOs = sentTransactions.stream()
                .map(this::convertToSentTransactionDTO)
                .collect(Collectors.toList());

        // Convertir las transacciones recibidas a DTO
        List<TransactionDTO.ReceivedTransactionDTO> receivedTransactionDTOs = receivedTransactions.stream()
                .map(this::convertToReceivedTransactionDTO)
                .collect(Collectors.toList());

        // Crear el DTO de transacciones
        return TransactionDTO.builder()
                .sent(sentTransactionDTOs)
                .received(receivedTransactionDTOs)
                .build();
    }

    // Método para convertir a DTO de transacción enviada
    private TransactionDTO.SentTransactionDTO convertToSentTransactionDTO(Transaction transaction) {
        return TransactionDTO.SentTransactionDTO.builder()
                .id(Math.toIntExact(transaction.getId()))
                .assetSymbol(transaction.getAssetSymbol())
                .amount(transaction.getQuantity())
                .pricePerUnit(transaction.getPricePerUnit())
                .type(transaction.getType().name()) // o transaction.getType().toString() si es un Enum
                .timestamp(transaction.getTimestamp().toString()) // Formato adecuado si es necesario
                .status(transaction.getStatus().name()) // o transaction.getStatus().toString() si es un Enum
                .fee(transaction.getFee())
                .senderWalletId(Math.toIntExact(transaction.getSenderWallet().getId()))
                .receiverWalletId(Math.toIntExact(transaction.getReceiverWallet().getId()))
                .build();
    }

    // Método para convertir a DTO de transacción recibida
    private TransactionDTO.ReceivedTransactionDTO convertToReceivedTransactionDTO(Transaction transaction) {
        return TransactionDTO.ReceivedTransactionDTO.builder()
                .id(Math.toIntExact(transaction.getId()))
                .assetSymbol(transaction.getAssetSymbol())
                .amount(transaction.getQuantity())
                .pricePerUnit(transaction.getPricePerUnit())
                .type(transaction.getType().name()) // o transaction.getType().toString() si es un Enum
                .timestamp(transaction.getTimestamp().toString()) // Formato adecuado si es necesario
                .status(transaction.getStatus().name()) // o transaction.getStatus().toString() si es un Enum
                .fee(transaction.getFee())
                .senderWalletId(Math.toIntExact(transaction.getSenderWallet().getId()))
                .receiverWalletId(Math.toIntExact(transaction.getReceiverWallet().getId()))
                .build();
    }
}
