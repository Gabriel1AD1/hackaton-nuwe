package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.service.BlockService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blockchain")
@AllArgsConstructor
public class BlockchainController {
    private final BlockService blockService;
    @PostMapping("/mine")
    public ResponseEntity<ResponseDTO> mineBlock() {
        return ResponseEntity.ok(blockService.mineBlock());
    }
}
