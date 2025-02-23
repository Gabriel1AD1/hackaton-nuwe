package com.hackathon.blockchain.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class MarketDataService {

    private final RestTemplate restTemplate;

    public MarketDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Obtener todos los precios
    public Map<String, Double> fetchLiveMarketPrices() {
        String apiUrl = "https://faas-lon1-917a94a7.doserverless.co/api/v1/web/fn-3d8ede30-848f-4a7a-acc2-22ba0cd9a382/default/fake-market-prices";
        return restTemplate.getForObject(apiUrl, Map.class);
    }

    // Obtener el precio de un activo específico
    public double fetchLivePriceForAsset(String symbol) {
        Map<String, Double> prices = fetchLiveMarketPrices();
        return prices.getOrDefault(symbol.toUpperCase(), -1.0); // Retorna -1 si el símbolo no existe
    }
}
