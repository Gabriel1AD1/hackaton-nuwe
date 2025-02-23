package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.model.Block;
import com.hackathon.blockchain.repository.BlockRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BlockchainService {
    private final BlockRepository blockRepository;
    private static final Logger log = LoggerFactory.getLogger(BlockchainService.class);

    public boolean isChainValid() {
        List<Block> chain = blockRepository.findAll(Sort.by(Sort.Direction.ASC, "blockIndex"));

        log.debug("Validating blockchain with {} blocks", chain.size());

        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);

            String recalculatedHash = current.calculateHash();
            if (!current.getHash().equals(recalculatedHash)) {
                log.error("❌ Hash mismatch in block {}", current.getBlockIndex());
                log.debug("Stored hash: {}", current.getHash());
                log.debug("Recalculated hash: {}", recalculatedHash);
                return false;
            }

            if (!current.getPreviousHash().equals(previous.getHash())) {
                log.error("❌ Previous hash mismatch in block {}", current.getBlockIndex());
                return false;
            }
        }

        log.info("✅ Blockchain is valid");
        return true;
    }
}
