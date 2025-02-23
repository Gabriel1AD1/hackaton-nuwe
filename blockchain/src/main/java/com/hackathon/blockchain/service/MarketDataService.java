package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.ResponseDTO;

import java.util.Map;

public interface MarketDataService {

    Map<String, Double> fetchLiveMarketPrices();

    double fetchLivePriceForAsset(String symbol);

    ResponseDTO fetchLivePriceForAssetResponse(String symbol);
}
