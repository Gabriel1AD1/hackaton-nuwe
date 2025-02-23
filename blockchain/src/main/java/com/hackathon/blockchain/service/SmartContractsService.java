package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.dto.SmartContractRequestDTO;
import com.hackathon.blockchain.dto.SmartContractResponseDTO;

public interface SmartContractsService {
    SmartContractResponseDTO newContract(SmartContractRequestDTO dto,Long userId);
    ResponseDTO isValid(Long smartContractId);
}
