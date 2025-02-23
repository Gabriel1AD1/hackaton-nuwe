package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.dto.*;
import com.hackathon.blockchain.enums.AccountStatus;
import com.hackathon.blockchain.enums.TransactionStatus;
import com.hackathon.blockchain.enums.TransactionType;
import com.hackathon.blockchain.exception.BadRequestException;
import com.hackathon.blockchain.exception.EntityNotFoundException;
import com.hackathon.blockchain.model.*;
import com.hackathon.blockchain.repository.*;

import com.hackathon.blockchain.service.MarketDataService;
import com.hackathon.blockchain.service.WalletService;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.security.*;
import java.time.Instant;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class WalletServiceI implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final SmartContractRepository smartContractRepository;
    private final MarketDataService marketDataService;
    private final UserRepository userRepository;
    private final WalletKeyRepository walletKeyRepository;
    private final AssetRepository assetRepository;
    private static final String KEY_DIR = "/keys/";

    public Optional<Wallet> getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId);
    }

    public Optional<Wallet> getWalletByAddress(String address) {
        return walletRepository.findByAddress(address);
    }

    @Transactional
    @Override
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

    /*
     * Los usuarios deben comprar primero USDT para poder cambiar por tokens
     * El dinero fiat no vale para comprar tokens
     * Cuando se intercambia USDT por cualquier moneda, no se añade USDT a los assets de otras monedas
     */
    @Transactional
    @Override
    public String buyAsset(Long userId, String symbol, double quantity) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);
        Optional<Wallet> liquidityWalletOpt = walletRepository.findByAddress("LP-" + symbol);
        Optional<Wallet> usdtLiquidityWalletOpt = walletRepository.findByAddress("LP-USDT");
    
        if (optionalWallet.isEmpty()) return "❌ Wallet not found!";
        if (liquidityWalletOpt.isEmpty()) return "❌ Liquidity pool for " + symbol + " not found!";
        if (usdtLiquidityWalletOpt.isEmpty()) return "❌ Liquidity pool for USDT not found!";
    
        Wallet userWallet = optionalWallet.get();
        Wallet liquidityWallet = liquidityWalletOpt.get();
        Wallet usdtLiquidityWallet = usdtLiquidityWalletOpt.get();
    
        double price = marketDataService.fetchLivePriceForAsset(symbol);
        double totalCost = quantity * price;
    
        if (symbol.equals("USDT")) {
            if (userWallet.getBalance() < totalCost) {
                return "❌ Insufficient fiat balance to buy USDT!";
            }
    
            userWallet.setBalance(userWallet.getBalance() - totalCost);
            updateWalletAssets(userWallet, "USDT", quantity);
            updateWalletAssets(usdtLiquidityWallet, "USDT", -quantity);
    
            walletRepository.save(userWallet);
            walletRepository.save(usdtLiquidityWallet);
    
            recordTransaction(usdtLiquidityWallet, userWallet, "USDT", quantity, price, TransactionType.BUY);
            return "✅ USDT purchased successfully!";
        }
    
        Optional<Asset> usdtAssetOpt = userWallet.getAssets().stream()
                .filter(a -> a.getSymbol().equals("USDT"))
                .findFirst();
    
        if (usdtAssetOpt.isEmpty() || usdtAssetOpt.get().getQuantity() < totalCost) {
            return "❌ Insufficient USDT balance! You must buy USDT first.";
        }
    
        updateWalletAssets(userWallet, "USDT", -totalCost);
        updateWalletAssets(usdtLiquidityWallet, "USDT", totalCost);
    
        updateWalletAssets(userWallet, symbol, quantity);
        updateWalletAssets(liquidityWallet, symbol, -quantity);
    
        walletRepository.save(userWallet);
        walletRepository.save(liquidityWallet);
        walletRepository.save(usdtLiquidityWallet);
    
        recordTransaction(liquidityWallet, userWallet, symbol, quantity, price, TransactionType.BUY);
    
        return "✅ Asset purchased successfully!";
    }

    /*
     * La venta siempre se hace por USDT
     * Los usuarios después pueden cambiar USDT por la moneda fiat
     */
    @Transactional
    @Override
    public String sellAsset(Long userId, String symbol, double quantity) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);
        Optional<Wallet> liquidityWalletOpt = walletRepository.findByAddress("LP-" + symbol);
    
        if (optionalWallet.isEmpty()) return "❌ Wallet not found!";
        if (liquidityWalletOpt.isEmpty()) return "❌ Liquidity pool for " + symbol + " not found!";
    
        Wallet userWallet = optionalWallet.get();
        Wallet liquidityWallet = liquidityWalletOpt.get();
    
        double price = marketDataService.fetchLivePriceForAsset(symbol);
        double totalRevenue = quantity * price;
    
        Optional<Asset> existingAsset = userWallet.getAssets().stream()
                .filter(a -> a.getSymbol().equals(symbol))
                .findFirst();
    
        if (existingAsset.isEmpty() || existingAsset.get().getQuantity() < quantity) {
            return "❌ Not enough assets to sell!";
        }
    
        // CASO 1: Venta de USDT (Recibo dinero fiat)
        if (symbol.equals("USDT")) {
            if (liquidityWallet.getAssets().stream().anyMatch(a -> a.getSymbol().equals("USDT") && a.getQuantity() < quantity)) {
                return "❌ Not enough USDT liquidity!";
            }
    
            userWallet.setBalance(userWallet.getBalance() + totalRevenue);
            updateWalletAssets(userWallet, symbol, -quantity);
            updateWalletAssets(liquidityWallet, symbol, quantity);
    
        } else {
            // CASO 2: Venta de otros assets (Recibo USDT)
            Optional<Wallet> usdtLiquidityWalletOpt = walletRepository.findByAddress("LP-USDT");
            if (usdtLiquidityWalletOpt.isEmpty()) return "❌ USDT liquidity pool not found!";
            Wallet usdtLiquidityWallet = usdtLiquidityWalletOpt.get();
    
            Optional<Asset> usdtAssetOpt = usdtLiquidityWallet.getAssets().stream()
                    .filter(a -> a.getSymbol().equals("USDT"))
                    .findFirst();
    
            if (usdtAssetOpt.isEmpty() || usdtAssetOpt.get().getQuantity() < totalRevenue) {
                return "❌ Not enough USDT in liquidity pool!";
            }
    
            updateWalletAssets(userWallet, "USDT", totalRevenue);
            updateWalletAssets(userWallet, symbol, -quantity);
            updateWalletAssets(usdtLiquidityWallet, "USDT", -totalRevenue);
            updateWalletAssets(liquidityWallet, symbol, quantity);
    
            walletRepository.save(usdtLiquidityWallet);
        }
    
        recordTransaction(userWallet, liquidityWallet, symbol, quantity, price, TransactionType.SELL);
    
        walletRepository.save(userWallet);
        walletRepository.save(liquidityWallet);
    
        return "✅ Asset sold successfully!";
    }

    @Override
    public WalletGenerateKeysDTO newKeyGenerate(Long userId) {
        return null;
    }

    /*
     * Esta versión ya no almacena purchasePrice en Assets
     */
    @Override
    public void updateWalletAssets(Wallet wallet, String assetSymbol, double amount) {
        Optional<Asset> assetOpt = wallet.getAssets().stream()
                .filter(asset -> asset.getSymbol().equalsIgnoreCase(assetSymbol))
                .findFirst();
    
        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            asset.setQuantity(asset.getQuantity() + amount);
            if (asset.getQuantity() <= 0) {
                wallet.getAssets().remove(asset);
            }
        } else if (amount > 0) {
            Asset newAsset = new Asset();
            newAsset.setSymbol(assetSymbol);
            newAsset.setQuantity(amount);
            newAsset.setWallet(wallet);
            wallet.getAssets().add(newAsset);
        }
    }     

    private void recordTransaction(Wallet sender, Wallet receiver, String assetSymbol, double quantity, double price, TransactionType type) {
        transactionRepository.save(Transaction.builder()
                        .senderWallet(sender)
                        .receiverWallet(receiver)
                        .assetSymbol(assetSymbol)
                        .quantity(quantity)
                        .pricePerUnit(price)
                        .type(type)
                        .timestamp(Instant.now())
                        .status(TransactionStatus.PENDING)
                        .fee(0.0)
                .build());
    }
    @Override
    public String createWalletForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("No existe el usuario")
        );
        Optional<Wallet> existingWallet = walletRepository.findByUserId(user.getId());
        if (existingWallet.isPresent()) {
            return "❌ You already have a wallet created.";
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .address(generateWalletAddress())
                .balance(10000.0)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        walletRepository.save(wallet);

        return "✅ Wallet successfully created! Address: " + wallet.getAddress();
    }

    private String generateWalletAddress() {
        return DigestUtils.sha256Hex(UUID.randomUUID().toString());
    }


    // Ejecuto esta función para tener patrimonios de carteras actualizados continuamente y que no contenga valores estáticos
    @Scheduled(fixedRate = 30000) // Se ejecuta cada 30 segundos
    @Transactional
    public void updateWalletBalancesScheduled() {
        log.info("🔄 Updating wallet net worths based on live market prices...");
    
        List<Wallet> wallets = walletRepository.findAll();
        for (Wallet wallet : wallets) {
            double totalValue = 0.0;
    
            for (Asset asset : wallet.getAssets()) {
                double marketPrice = marketDataService.fetchLivePriceForAsset(asset.getSymbol());
                double assetValue = asset.getQuantity() * marketPrice;
                totalValue += assetValue;
    
                log.info("💰 Asset {} - Quantity: {} - Market Price: {} - Total Value: {}",
                        asset.getSymbol(), asset.getQuantity(), marketPrice, assetValue);
            }
    
            if (wallet.getUser() != null) {
                totalValue += wallet.getBalance();
            }
    
            double previousNetWorth = wallet.getNetWorth();
            wallet.setNetWorth(totalValue);
            walletRepository.save(wallet);
    
            log.info("📊 Wallet [{}] - Previous Net Worth: {} - Updated Net Worth: {}",
                    wallet.getAddress(), previousNetWorth, totalValue);
    
            Wallet savedWallet = walletRepository.findById(wallet.getId()).orElse(null);
            if (savedWallet != null) {
                log.info("✅ Confirmed DB Update - Wallet [{}] New Net Worth: {}", savedWallet.getAddress(), savedWallet.getNetWorth());
            } else {
                log.error("❌ Failed to fetch wallet [{}] after update!", wallet.getAddress());
            }
        }
    
        log.info("✅ All wallet net worths updated successfully!");
    }
    @Override
    public Map<String, Object> getWalletBalance(Long userId) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);
    
        if (optionalWallet.isEmpty()) {
            return Map.of("error", "Wallet not found");
        }
    
        Wallet wallet = optionalWallet.get();
        Map<String, Double> assetPrices = marketDataService.fetchLiveMarketPrices();
    
        Map<String, Double> assetsMap = new HashMap<>();
        double netWorth = wallet.getBalance();
    
        for (Asset asset : wallet.getAssets()) {
            double currentPrice = assetPrices.getOrDefault(asset.getSymbol(), 0.0);
            double assetValue = asset.getQuantity() * currentPrice;
            assetsMap.put(asset.getSymbol(), assetValue);
            netWorth += assetValue;
        }
    
        Map<String, Object> walletInfo = new HashMap<>();
        walletInfo.put("wallet_address", wallet.getAddress());
        walletInfo.put("cash_balance", wallet.getBalance());
        walletInfo.put("net_worth", netWorth);
        walletInfo.put("assets", assetsMap);
    
        return walletInfo;
    }
    
    /**
     * Devuelve un mapa con dos listas de transacciones:
     * - "sent": transacciones enviadas (donde la wallet es remitente)
     * - "received": transacciones recibidas (donde la wallet es destinataria)
     */
    @Override
    public Map<String, List<Transaction>> getWalletTransactions(Long walletId) {
        Optional<Wallet> walletOpt = walletRepository.findById(walletId);
        if (walletOpt.isEmpty()) {
            return Map.of("error", List.of());
        }
        Wallet wallet = walletOpt.get();
        List<Transaction> sentTransactions = transactionRepository.findBySenderWallet(wallet);
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverWallet(wallet);
        Map<String, List<Transaction>> result = new HashMap<>();
        result.put("sent", sentTransactions);
        result.put("received", receivedTransactions);
        return result;
    }

    // RETO BACKEND

    // Método para transferir el fee: deducirlo del wallet del emisor y sumarlo a la wallet de fees.
    @Override
    public void transferFee(Transaction tx, double fee) {
        Wallet sender = tx.getSenderWallet();
        // Supongamos que el liquidity pool de USDT (o la wallet designada para fees) tiene ID 2.
        Optional<Wallet> feeWalletOpt = walletRepository.findByAddress("FEES-USDT");
        if (feeWalletOpt.isPresent()) {
            Wallet feeWallet = feeWalletOpt.get();
            // Actualiza los balances:
            sender.setBalance(sender.getBalance() - fee);
            feeWallet.setBalance(feeWallet.getBalance() + fee);
            walletRepository.save(sender);
            walletRepository.save(feeWallet);
        }
    }

    // Método para crear una wallet para fees (solo USDT)
    @Override
    public String createFeeWallet() {
        String feeWalletAddress = "FEES-USDT";
        Optional<Wallet> existing = walletRepository.findByAddress(feeWalletAddress);
        if (existing.isPresent()) {
            return "Fee wallet already exists with address: " + feeWalletAddress;
        }
        Wallet feeWallet = new Wallet();
        feeWallet.setAddress(feeWalletAddress);
        feeWallet.setBalance(0.0);
        feeWallet.setAccountStatus(AccountStatus.ACTIVE);
        // Al no estar asociada a un usuario, se deja user en null
        walletRepository.save(feeWallet);
        return "Fee wallet created successfully with address: " + feeWalletAddress;
    }
    @Override
    public Wallet findByUser(String username) {
        return null;

    }

    @Override
    public WalletGenerateKeysDTO generateKeys(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found for user"));
        var responseDb = walletKeyRepository.findByWallet(wallet).orElseGet(() -> {

            // Generar claves RSA
            KeyPairGenerator keyGen;
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();

            String publicKeyStr = encodeKey(pair.getPublic());
            String privateKeyStr = encodeKey(pair.getPrivate());

            // Guardar en archivos
            try {
                saveKeyToFile(wallet.getId(), "private.pem", privateKeyStr);
            } catch (Exception e) {
                throw new RuntimeException("Error generating keys", e);
            }
            try {
                saveKeyToFile(wallet.getId(), "public.pem", publicKeyStr);
            } catch (Exception e) {
                throw new RuntimeException("Error generating keys", e);
            }

            // Crear y guardar con Builder
            WalletKey newWalletKey = WalletKey.builder()
                    .wallet(wallet)
                    .publicKey(publicKeyStr)
                    .privateKey(privateKeyStr)
                    .build();

            return walletKeyRepository.save(newWalletKey);

        });
        // Verificar si ya existen claves
        return WalletGenerateKeysDTO.generateKey(responseDb.getPublicKey(), "/abs/path/to/keys",wallet.getId());
    }

    @Override
    public Map<String, Double> fetchLiveMarketPrices() {
        return marketDataService.fetchLiveMarketPrices();
    }

    @Override
    public ResponseDTO fetchLivePriceForAsset(String symbol) {

        double price = marketDataService.fetchLivePriceForAsset(symbol);
        if (price == -1){
            throw new EntityNotFoundException("Asset not found");
        }
        return ResponseDTO.createMessageForPriceAsset(symbol,price);
    }

    @Override
    @Transactional
    public WalletBuyResponseDTO walletBuy(WalletBuyRequestDTO dto,Long userId) {
        // 1. Obtener la billetera del usuario autenticado
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("❌ User not found"));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("❌ Wallet not found"));

        // 2. Verificar si hay un smart contract asociado al activo
        Optional<SmartContract> smartContract = smartContractRepository.findByName(dto.getSymbol());
        if (smartContract.isPresent()) {
            boolean isBlocked = evaluateSmartContract(smartContract.get(), dto.getSymbol(),dto.getQuantity(), wallet);
            if (isBlocked) {
                throw new BadRequestException(WalletBuyResponseDTO.error(dto.getSymbol()).getMessage());
            }
        }

        // 3. Obtener el precio de la criptomoneda
        double pricePerUnit = marketDataService.fetchLivePriceForAsset(dto.getSymbol());

        // 4. Calcular el costo total
        double totalCost = pricePerUnit * dto.getQuantity();

        // 5. Verificar si la billetera tiene saldo suficiente
        if (wallet.getBalance() < totalCost) {
            throw new BadRequestException(WalletBuyResponseDTO.insufficientBalanceBuy(dto.getSymbol()).getMessage());
        }

        // 6. Descontar saldo y registrar la transacción
        wallet.setBalance(wallet.getBalance() - totalCost);

        // Registrar la compra como transacción
        Transaction transaction = Transaction.builder()
                .senderWallet(wallet)
                .receiverWallet(wallet) // La billetera se autoenvía el activo
                .assetSymbol(dto.getSymbol())
                .quantity(dto.getQuantity())
                .pricePerUnit(pricePerUnit)
                .block("")
                .type(TransactionType.BUY)
                .timestamp(Instant.now())
                .status(TransactionStatus.COMPLETED)
                .fee(0.0)
                .amount(BigDecimal.valueOf(totalCost))
                .build();

        transactionRepository.save(transaction);

        // 7. Guardar cambios en la billetera
        walletRepository.save(wallet);

        return WalletBuyResponseDTO.success();
    }
    @Override
    @Transactional
    public WalletSellResponseDTO walletSell(WalletSellRequestDTO dto, Long userId) {
        // 1. Obtener la billetera del usuario autenticado
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("❌ User not found"));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("❌ Wallet not found"));

        // 2. Verificar si hay un smart contract asociado al activo
        Optional<SmartContract> smartContract = smartContractRepository.findByName(dto.getSymbol());
        if (smartContract.isPresent()) {
            boolean isBlocked = evaluateSmartContract(smartContract.get(), dto.getSymbol(), dto.getQuantity(), wallet);
            if (isBlocked) {
                throw new BadRequestException(WalletSellResponseDTO.error(dto.getSymbol()).getMessage());
            }
        }

        // 3. Buscar el activo en la billetera
        Asset asset = wallet.getAssets().stream()
                .filter(a -> a.getSymbol().equals(dto.getSymbol()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(WalletSellResponseDTO.insufficientAssets(dto.getSymbol()).getMessage()));

        // 4. Verificar si hay suficiente cantidad para vender
        if (asset.getQuantity() < dto.getQuantity()) {
            throw new BadRequestException(WalletSellResponseDTO.insufficientAssets(dto.getSymbol()).getMessage());
        }

        // 5. Obtener el precio actual del mercado
        double pricePerUnit = marketDataService.fetchLivePriceForAsset(dto.getSymbol());
        double totalSale = pricePerUnit * dto.getQuantity();

        // 6. Actualizar la cantidad del activo
        asset.setQuantity(asset.getQuantity() - dto.getQuantity());

        // Si la cantidad llega a 0, eliminar el activo de la billetera
        if (asset.getQuantity() == 0) {
            wallet.getAssets().remove(asset);
            assetRepository.delete(asset);
        }

        // 7. Actualizar el balance de la billetera
        wallet.setBalance(wallet.getBalance() + totalSale);

        // 8. Registrar la venta como transacción
        Transaction transaction = Transaction.builder()
                .senderWallet(wallet)
                .receiverWallet(wallet) // Se autoenvía la venta
                .assetSymbol(dto.getSymbol())
                .quantity(dto.getQuantity())
                .pricePerUnit(pricePerUnit)
                .type(TransactionType.SELL)
                .timestamp(Instant.now())
                .status(TransactionStatus.MINED)
                .fee(0.0)
                .amount(BigDecimal.valueOf(totalSale))
                .build();

        transactionRepository.save(transaction);

        // 9. Guardar cambios en la billetera
        walletRepository.save(wallet);

        return WalletSellResponseDTO.success();
    }

    private boolean evaluateSmartContract(SmartContract contract, String symbol,Double quantity, Wallet wallet) {
        // Aquí se evaluaría la condición definida en el contrato inteligente.
        // En una implementación real, esto podría interpretarse con un motor de reglas.
        return false;
    }

    private String encodeKey(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    private String encodeKey(PrivateKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    private void saveKeyToFile(Long walletId, String fileName, String keyContent) throws Exception {
        String filePath = Paths.get(KEY_DIR, "wallet_" + walletId + "_" + fileName).toString();
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(keyContent);
        }
    }

}