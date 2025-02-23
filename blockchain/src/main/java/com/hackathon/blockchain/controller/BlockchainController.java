package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.BlockResponseDTO;
import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.service.BlockService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/blockchain")
@AllArgsConstructor
public class BlockchainController {
    private final BlockService blockService;
    @PostMapping("/mine")
    public ResponseEntity<ResponseDTO> mineBlock() {
        return ResponseEntity.ok(blockService.mineBlock());
    }
    @GetMapping
    public ResponseEntity<List<BlockResponseDTO>> getMinesBlock(){
        return ResponseEntity.ok(blockService.findAllBlock());
    }
    @GetMapping("/valid")
    public ResponseEntity<ResponseDTO> isValidBlocks(){
        return ResponseEntity.ok(blockService.isValidBlock());
    }
}
