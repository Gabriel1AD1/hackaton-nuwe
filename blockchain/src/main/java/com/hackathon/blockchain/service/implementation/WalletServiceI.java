package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.dto.*;
import com.hackathon.blockchain.enums.AccountStatus;
import com.hackathon.blockchain.enums.TransactionStatus;
import com.hackathon.blockchain.enums.TransactionType;
import com.hackathon.blockchain.exception.BadRequestException;
import com.hackathon.blockchain.exception.EntityNotFoundException;
import com.hackathon.blockchain.model.*;
import com.hackathon.blockchain.repository.*;
import com.hackathon.blockchain.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class WalletServiceI implements WalletService {
    private final BlockRepository blockRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final SmartContractRepository smartContractRepository;
    private final MarketDataServiceI marketDataServiceI;
    private final UserRepository userRepository;
    private final WalletKeyRepository walletKeyRepository;
    private final AssetRepository assetRepository;
    private static final String KEY_DIR = "keys";

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
            throw new BadRequestException("❌ You already have a wallet created.");
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(1000000.0)
                .accountStatus(AccountStatus.ACTIVE)
                .address("LP-BTC")
                .build();

        walletRepository.save(wallet);

        return "✅ Wallet successfully created! Address: " + wallet.getAddress();
    }


    @Override
    @Transactional(readOnly = true)
    public WalletBalanceDTO getWalletBalance(Long userId) {
        Optional<Wallet> optionalWallet = walletRepository.findByUserId(userId);

        Wallet wallet = getWallet(optionalWallet);
        Map<String, Double> assetPrices = getAssetPrices();

        Map<String, Double> assetsMap = new HashMap<>();
        double netWorth = wallet.getBalance();

        for (Asset asset : wallet.getAssets()) {
            double currentPrice = assetPrices.getOrDefault(asset.getSymbol(), 0.0);
            double assetValue = asset.getQuantity() * currentPrice;
            assetsMap.put(asset.getSymbol(), assetValue);
            netWorth += assetValue;
        }

        // Usar el builder para crear la instancia de WalletBalanceDTO
        return WalletBalanceDTO.builder()
                .walletAddress(wallet.getAddress())
                .cashBalance(wallet.getBalance())
                .netWorth(netWorth)
                .assets(assetsMap)
                .build();
    }

    private Map<String, Double> getAssetPrices() {
        return marketDataServiceI.fetchLiveMarketPrices();
    }

    private static Wallet getWallet(Optional<Wallet> optionalWallet) {
        return optionalWallet.orElseThrow(
                () -> new EntityNotFoundException("Wallet not found")

        );
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


    @Override
    @Transactional
    public WalletBuyResponseDTO walletBuy(WalletBuyRequestDTO dto, Long userId) {
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
                throw new BadRequestException(WalletBuyResponseDTO.error(dto.getSymbol()).getMessage());
            }
        }

        // 3. Obtener el precio de la criptomoneda
        double pricePerUnit = marketDataServiceI.fetchLivePriceForAsset(dto.getSymbol());

        // 4. Calcular el costo total
        double totalCost = pricePerUnit * dto.getQuantity();

        // 5. Verificar si la billetera tiene saldo suficiente
        if (wallet.getBalance() < totalCost) {
            throw new BadRequestException(WalletBuyResponseDTO.insufficientBalanceBuy(dto.getSymbol()).getMessage());
        }

        // 6. Descontar saldo y registrar la transacción
        wallet.setBalance(wallet.getBalance() - totalCost);

        // Crear el nuevo bloque
        Block newBlock = Block.builder()
                .blockIndex(getNextBlockIndex()) // Implementa un método para obtener el siguiente índice
                .previousHash(getLastBlockHash()) // Implementa un método para obtener el hash del último bloque
                .isGenesis(false)
                .timestamp(LocalDateTime.now())
                .nonce(0) // Inicialmente 0, se actualizará durante la minería
                .build();
        newBlock.setHash(newBlock.calculateHash());
        // Registrar la compra como transacción
        Transaction transaction = Transaction.builder()
                .senderWallet(wallet)
                .receiverWallet(wallet) // La billetera se autoenvía el activo
                .assetSymbol(dto.getSymbol())
                .quantity(dto.getQuantity())
                .pricePerUnit(pricePerUnit)
                .type(TransactionType.BUY)
                .timestamp(Instant.now())
                .status(TransactionStatus.PENDING) // Marca como pendiente
                .fee(0.0)
                .amount(BigDecimal.valueOf(totalCost))
                .build();
        // Guarda la transacción en la base de datos
        transactionRepository.save(transaction);


        // Guarda el bloque en la base de datos
        blockRepository.save(newBlock);

        // 7. Guardar cambios en la billetera
        walletRepository.save(wallet);

        return WalletBuyResponseDTO.success();
    }

    private int getNextBlockIndex() {
        Optional<Block> lastBlock = blockRepository.findTopByOrderByBlockIndexDesc();
        return lastBlock.map(block -> block.getBlockIndex() + 1).orElse(0);
    }

    private String getLastBlockHash() {
        Optional<Block> lastBlock = blockRepository.findTopByOrderByBlockIndexDesc();
        return lastBlock.map(Block::getHash).orElse("0");
    }

    private boolean evaluateSmartContract(SmartContract contract, String symbol,Double quantity, Wallet wallet) {
        // Aquí se evaluaría la condición definida en el contrato inteligente.
        // En una implementación real, esto podría interpretarse con un motor de reglas.
        return false;
    }



}