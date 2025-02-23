package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.BlockResponseDTO;
import com.hackathon.blockchain.dto.ResponseDTO;

import java.util.List;

public interface BlockService {
    ResponseDTO mineBlock();

    List<BlockResponseDTO> findAllBlock();

    ResponseDTO isValidBlock();
}
