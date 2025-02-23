package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.enums.TransactionStatus;
import com.hackathon.blockchain.exception.BadRequestException;
import com.hackathon.blockchain.model.Block;
import com.hackathon.blockchain.model.Transaction;
import com.hackathon.blockchain.repository.BlockRepository;
import com.hackathon.blockchain.repository.TransactionRepository;
import com.hackathon.blockchain.service.BlockService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BlockServiceI implements BlockService{
    private final TransactionRepository transactionRepository;
    private final BlockRepository blockRepository;
    @Override
    public ResponseDTO mineBlock() {
        List<Transaction> pendingTransactions = transactionRepository.getPendingTransactions(TransactionStatus.PENDING);

        // Verificar si hay transacciones pendientes
        if (pendingTransactions.isEmpty()) {
            throw new BadRequestException(ResponseDTO.noPendingTransactionMine().getMessage());
        }

        // Crear el nuevo bloque
        Block newBlock = new Block();
        newBlock.setBlockIndex((int) (blockRepository.count() + 1)); // Asignar el índice del bloque
        newBlock.setPreviousHash(blockRepository.findTopByOrderByBlockIndexDesc().getHash()); // Obtener el hash del bloque anterior
        newBlock.setData(pendingTransactions.toString()); // Puedes personalizar cómo representar los datos
        newBlock.setTimestamp(LocalDateTime.now());

        // Proceso de minería: encontrar un nonce válido
        int nonce = 0;
        String blockHash;
        do {
            newBlock.setNonce(nonce);
            blockHash = newBlock.calculateHash();
            nonce++;
        } while (!blockHash.startsWith("0000"));

        // Establecer el hash final del bloque
        newBlock.setHash(blockHash);

        // Guardar el bloque en la base de datos
        blockRepository.save(newBlock);

        // Limpiar transacciones que se han incluido en el bloque
        transactionRepository.clearPendingTransactions(pendingTransactions,TransactionStatus.COMPLETED);
        return ResponseDTO.simulateMiningMessage(blockHash);
    }

}
