package com.hackathon.blockchain.controller;

import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.service.MarketDataService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/market")
@AllArgsConstructor
public class MarketController {
    private final MarketDataService marketDataServiceI;

    @GetMapping("/prices")
    public ResponseEntity<Map<String,Double>> getMarketPrices() {
        return ResponseEntity.ok(marketDataServiceI.fetchLiveMarketPrices());
    }
    @GetMapping("/price/{symbol}")
    public ResponseEntity<ResponseDTO> getAssetPrice(@PathVariable String symbol) {
        return ResponseEntity.ok(marketDataServiceI.fetchLivePriceForAssetResponse(symbol));
    }
}
