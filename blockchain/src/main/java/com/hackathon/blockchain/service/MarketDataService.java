package com.hackathon.blockchain.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MarketDataService {

    private final String apiUrl = "https://faas-lon1-917a94a7.doserverless.co/api/v1/web/fn-3d8ede30-848f-4a7a-acc2-22ba0cd9a382/default/fake-market-prices";

    public double fetchLivePriceForAsset(String symbol) {
        return 0.0;
    }

    public Map<String, Double> fetchLiveMarketPrices() {
        return Map.of();
    }
}