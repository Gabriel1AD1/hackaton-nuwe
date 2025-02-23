package com.hackathon.blockchain.seeder;

import com.hackathon.blockchain.model.Asset;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.enums.AccountStatus;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.MarketDataService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
public class WalletSeeder implements CommandLineRunner {

    private final WalletRepository walletRepository;
    private final MarketDataService marketDataService;


    @Override
    public void run(String... args) {
        initializeLiquidityPools(marketDataService.fetchLiveMarketPrices());
    }
    public void initializeLiquidityPools(Map<String, Double> initialAssets) {
        for (Map.Entry<String, Double> entry : initialAssets.entrySet()) {
            String symbol = entry.getKey();
            double initialQuantity = entry.getValue();

            String liquidityWalletAddress = "LP-" + symbol;
            Optional<Wallet> existingWallet = walletRepository.findByAddress(liquidityWalletAddress);

            if (existingWallet.isEmpty()) {
                Wallet liquidityWallet = new Wallet();
                liquidityWallet.setAddress(liquidityWalletAddress);
                liquidityWallet.setBalance(0.0);
                walletRepository.save(liquidityWallet);

                Asset asset = new Asset(null, symbol, initialQuantity, 0, liquidityWallet);
                liquidityWallet.getAssets().add(asset);
            }
        }
    }

}