package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.SmartContractRequestDTO;
import com.hackathon.blockchain.service.SmartContractsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contracts")
@AllArgsConstructor
public class SmartContractsController extends ControllerBase{
    private final SmartContractsService smartContractsService;
    @PostMapping("/create")
    public ResponseEntity<?> newContract(@RequestBody SmartContractRequestDTO dto){
        return ResponseEntity.ok(smartContractsService.newContract(dto, getUserSessionSecurity().getUserId()));
    }
    @PostMapping("/validate/{id-contract}")
    public ResponseEntity<?> isValidContract(@PathVariable("id-contract") Long parameter){
        return ResponseEntity.ok().build();
    }
}
