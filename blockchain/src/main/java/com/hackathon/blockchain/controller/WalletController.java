package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.*;
import com.hackathon.blockchain.service.TransactionService;
import com.hackathon.blockchain.service.WalletKeyService;
import com.hackathon.blockchain.service.WalletService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallet")
@AllArgsConstructor
public class WalletController extends ControllerBase {
    private final WalletService walletService;
    private final WalletKeyService walletKeyService;
    private final TransactionService transactionService;
    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> newWallet(){
        String response = walletService.createWalletForUser(getUserSessionSecurity().getUserId());
        return ResponseEntity.ok(ResponseDTO.createWallerMessage(response));
    }
    @PostMapping("/generate-keys")
    public ResponseEntity<WalletGenerateKeysDTO> generateKeys()  {
        return ResponseEntity.ok(walletKeyService.generateAndStoreKeys(getUserSessionSecurity().getUserId()));
    }
    @PostMapping("/buy")
    public ResponseEntity<WalletBuyResponseDTO> buyWallet(@Valid  @RequestBody WalletBuyRequestDTO dto){
        return ResponseEntity.ok(walletService.walletBuy(dto, getUserSessionSecurity().getUserId()));
    }
    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceDTO> getWalletBalance(){
        return ResponseEntity.ok(walletService.getWalletBalance(getUserSessionSecurity().getUserId()));
    }
    @GetMapping("/transactions")
    public ResponseEntity<TransactionDTO> getTransaction(){
        return ResponseEntity.ok(transactionService.getTransactionForUserId(getUserSessionSecurity().getUserId()));
    }
}
