package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.ApiResponse;
import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.service.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
@AllArgsConstructor
public class WalletController extends ControllerBase {
    private WalletService walletService;
    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> newWallet(){
        String response = walletService.createWalletForUser(getUserSessionSecurity().getUserId());
        return ResponseEntity.ok(ResponseDTO.createWallerMessage(response));
    }
}
