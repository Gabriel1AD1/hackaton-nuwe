package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.dto.BlockResponseDTO;
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
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<BlockResponseDTO> findAllBlock() {
        List<Block> blocks = blockRepository.findAll();
        // Suponiendo que el bloque 0 es el génesis
        return blocks.stream()
                .map(block -> BlockResponseDTO.builder()
                        .id(block.getId())
                        .blockIndex(block.getBlockIndex())
                        .timestamp(block.getTimestamp().toEpochSecond(ZoneOffset.UTC)) // Convertir a long en segundos
                        .previousHash(block.getPreviousHash())
                        .nonce(block.getNonce())
                        .hash(block.getHash())
                        .genesis(block.getBlockIndex() == 0) // Suponiendo que el bloque 0 es el génesis
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ResponseDTO isValidBlock() {
        List<Block> blocks = blockRepository.findAll();
        boolean isValid = true;

        for (int i = 1; i < blocks.size(); i++) {
            Block currentBlock = blocks.get(i);
            Block previousBlock = blocks.get(i - 1);

            // Verifica si el hash del bloque actual coincide con el hash calculado
            String calculatedHash = currentBlock.calculateHash();
            if (!currentBlock.getHash().equals(calculatedHash)) {
                isValid = false;
                break;
            }

            // Verifica si el hash del bloque anterior coincide con el hash almacenado en el bloque actual
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                isValid = false;
                break;
            }
        }

        return isValid ? ResponseDTO.blockChainIsValid() : ResponseDTO.blockChainIsNotValid();
    }

}
