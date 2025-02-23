package com.hackathon.blockchain.schedule;

import com.hackathon.blockchain.model.Asset;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.MarketDataService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class WalletBalanceSchedule {
    private static final Logger log = LoggerFactory.getLogger(WalletBalanceSchedule.class);
    private final MarketDataService marketDataServiceI;
    private final WalletRepository walletRepository;

    @Scheduled(fixedRate = 30000) // Se ejecuta cada 30 segundos
    @Transactional
    public void updateWalletBalancesScheduled() {
        log.info("🔄 Actualizando patrimonios de carteras según precios de mercado en tiempo real...");
        List<Wallet> wallets = walletRepository.findAll();
        for (Wallet wallet : wallets) {
            double totalValue = calculateTotalValue(wallet);
            totalValue += Optional.ofNullable(wallet.getUser())
                    .map(user -> wallet.getBalance())
                    .orElse(0.0);

            updateWalletNetWorth(wallet, totalValue);
        }

        log.info("✅ ¡Todos los patrimonios de las carteras se actualizaron con éxito!");
    }

    private double calculateTotalValue(Wallet wallet) {
        return wallet.getAssets().stream()
                .mapToDouble(this::calculateAssetValue)
                .sum();
    }

    private double calculateAssetValue(Asset asset) {
        double marketPrice = marketDataServiceI.fetchLivePriceForAsset(asset.getSymbol());
        double assetValue = asset.getQuantity() * marketPrice;

        logAssetInfo(asset, marketPrice, assetValue);
        return assetValue;
    }

    private void logAssetInfo(Asset asset, double marketPrice, double assetValue) {
        log.info("💰 Activo {} - Cantidad: {} - Precio de Mercado: {} - Valor Total: {}",
                asset.getSymbol(), asset.getQuantity(), marketPrice, assetValue);
    }

    private void updateWalletNetWorth(Wallet wallet, double totalValue) {
        double previousNetWorth = wallet.getNetWorth();
        wallet.setNetWorth(totalValue);
        walletRepository.save(wallet);

        logUpdatedNetWorth(wallet, previousNetWorth);
    }

    private void logUpdatedNetWorth(Wallet wallet, double previousNetWorth) {
        log.info("📊 Cartera [{}] - Patrimonio Anterior: {} - Patrimonio Actualizado: {}",
                wallet.getAddress(), previousNetWorth, wallet.getNetWorth());

        Optional<Wallet> savedWallet = walletRepository.findById(wallet.getId());
        savedWallet.ifPresentOrElse(
                w -> log.info("✅ Confirmada actualización en DB - Cartera [{}] Nuevo Patrimonio: {}", w.getAddress(), w.getNetWorth()),
                () -> log.error("❌ ¡Error al obtener la cartera [{}] después de la actualización!", wallet.getAddress())
        );
    }

}
