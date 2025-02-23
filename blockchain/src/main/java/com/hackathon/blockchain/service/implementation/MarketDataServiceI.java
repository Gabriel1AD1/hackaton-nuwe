package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.exception.BadRequestException;
import com.hackathon.blockchain.service.MarketDataService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
@Service
public class MarketDataServiceI implements MarketDataService {
    private final RestTemplate restTemplate;

    public MarketDataServiceI(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, Double> fetchLiveMarketPrices() {
        String apiUrl = "https://faas-lon1-917a94a7.doserverless.co/api/v1/web/fn-3d8ede30-848f-4a7a-acc2-22ba0cd9a382/default/fake-market-prices";
        return restTemplate.getForObject(apiUrl, Map.class);
    }

    @Override
    public double fetchLivePriceForAsset(String symbol) {
        Map<String, Double> prices = fetchLiveMarketPrices();
        return prices.getOrDefault(symbol.toUpperCase(), -1.0);
    }

    @Override
    public ResponseDTO fetchLivePriceForAssetResponse(String symbol) {
        double price = this.fetchLivePriceForAsset(symbol);
        if (price == -1) {
            throw new BadRequestException("❌ Asset not found or price unavailable: " + symbol);
        }
        return ResponseDTO.createMessageForPriceAsset(symbol, price);
    }
}
