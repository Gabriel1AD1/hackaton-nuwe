package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.dto.WalletBuyRequestDTO;
import com.hackathon.blockchain.dto.WalletBuyResponseDTO;
import com.hackathon.blockchain.dto.WalletGenerateKeysDTO;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.model.WalletKey;
import com.hackathon.blockchain.service.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    @PostMapping("/generate-keys")
    public ResponseEntity<WalletGenerateKeysDTO> generateKeys()  {
        return ResponseEntity.ok(walletService.generateKeys(getUserSessionSecurity().getUserId()));
    }
    @GetMapping("/market/prices")
    public ResponseEntity<Map<String,Double>> getMarketPrices() {
        return ResponseEntity.ok(walletService.fetchLiveMarketPrices());
    }
    @GetMapping("/price/{symbol}")
    public ResponseEntity<ResponseDTO> getAssetPrice(@PathVariable String symbol) {
        return ResponseEntity.ok(walletService.fetchLivePriceForAsset(symbol));
    }
    @PostMapping("/buy")
    public ResponseEntity<WalletBuyResponseDTO> buyWallet(@RequestBody WalletBuyRequestDTO dto){
        return ResponseEntity.ok(walletService.walletBuy(dto, getUserSessionSecurity().getUserId()));
    }
}
