package com.hackathon.blockchain.seeder;

import com.hackathon.blockchain.enums.AccountStatus;
import com.hackathon.blockchain.model.Asset;
import com.hackathon.blockchain.model.User;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.AssetRepository;
import com.hackathon.blockchain.repository.UserRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final WalletRepository walletRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    public DataInitializer(WalletRepository walletRepository, AssetRepository assetRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        createInitialWallets();
    }

    private void createInitialWallets() {
        List<User> users = userRepository.findAll();
        List<String> assetSymbols = Arrays.asList("BTC", "ETH", "USDT", "NCOIN", "CCOIN");
        List<Double> initialQuantities = Arrays.asList(100000.0, 400000.0, 1000000.0, 10000000.0, 2000000.0);

        for (var user : users){
            for (int i = 0; i < assetSymbols.size(); i++) {
                String symbol = assetSymbols.get(i);
                double quantity = initialQuantities.get(i);
                Wallet wallet = walletRepository.findByUserId(user.getId()).orElseGet(
                        () ->{
                                var walletSave = Wallet.builder()
                                        .address(generateWalletAddress()) // Generar la dirección del wallet
                                        .balance(0) // Inicialmente sin saldo
                                        .netWorth(0) // Inicialmente sin valor neto
                                        .accountStatus(AccountStatus.ACTIVE)
                                        .user(user)
                                        .build();

                            return walletRepository.save(walletSave); // Guardar el wallet
                        }
                );
                if (!assetRepository.existsBySymbolAndWalletId("LP-" + symbol,wallet.getId())){
                    // Crear el asset asociado al wallet
                    Asset asset = new Asset();
                    asset.setWallet(wallet);
                    asset.setSymbol("LP-" + symbol);
                    asset.setQuantity(quantity);
                    assetRepository.save(asset); // Guardar el asset
                }

            }
        }

    }
    private String generateWalletAddress() {
        return DigestUtils.sha256Hex(UUID.randomUUID().toString());
    }

}