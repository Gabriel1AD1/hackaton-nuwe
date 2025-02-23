package com.hackathon.blockchain.seeder;

import com.hackathon.blockchain.model.Block;
import com.hackathon.blockchain.repository.BlockRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@AllArgsConstructor
public class BlockSeeder {

    private final BlockRepository blockRepository;

    @PostConstruct
    public void init() {
        createGenesisBlock();
    }

    private void createGenesisBlock() {
        if (blockRepository.count() == 0) {
            Block genesisBlock = Block.builder()
                    .blockIndex(0)
                    .previousHash("0") // Hash inicial para el bloque génesis
                    .data("") // Sin datos asociados
                    .timestamp(LocalDateTime.now())
                    .nonce(0) // El nonce inicial puede ser 0
                    .hash("") // Inicializa el hash como vacío
                    .build();

            // Calcula el hash del bloque génesis
            genesisBlock.setHash(genesisBlock.calculateHash());

            // Guarda el bloque génesis en la base de datos
            blockRepository.save(genesisBlock);
        }
    }
}
