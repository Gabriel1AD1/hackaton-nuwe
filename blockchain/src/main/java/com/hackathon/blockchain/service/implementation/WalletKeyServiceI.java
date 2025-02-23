package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.dto.WalletGenerateKeysDTO;
import com.hackathon.blockchain.exception.EntityNotFoundException;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.model.WalletKey;
import com.hackathon.blockchain.repository.WalletKeyRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.WalletKeyService;
import com.hackathon.blockchain.utils.PemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

import static com.hackathon.blockchain.utils.EncodeUtils.encodeKey;

@Service
public class WalletKeyServiceI implements WalletKeyService {

    private static final String KEYS_FOLDER = "keys";
    private static final Logger log = LoggerFactory.getLogger(WalletKeyServiceI.class);
    private final WalletKeyRepository walletKeyRepository;
    private final WalletRepository walletRepository;

    public WalletKeyServiceI(WalletKeyRepository walletKeyRepository, WalletRepository walletRepository) {
        this.walletKeyRepository = walletKeyRepository;
        this.walletRepository = walletRepository;
        // Asegurarse de que la carpeta /keys exista
        File dir = new File(KEYS_FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        log.debug("Directorio de claves: {}", dir.getAbsolutePath());
    }

    @Override
    /**
     * Genera un par de claves RSA de 2048 bits, las convierte a PEM y las almacena en archivos,
     * además de guardarlas en la base de datos vinculadas a la wallet.
     */
    @Transactional
    public WalletGenerateKeysDTO generateAndStoreKeys(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found for user"));

        var responseDb = walletKeyRepository.findByWallet(wallet).orElseGet(() -> {

            // Generar claves RSA
            KeyPairGenerator keyGen;
            keyGen = getKeyPairGenerator();
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();

            Path privateKeyPath = getPrivateKeyPath(wallet);
            Path publicKeyPath = getPublicKeyPath(wallet);


            // Convertir las claves a formato PEM
            String publicKeyPEM = PemUtil.toPEMFormatPublic(encodeKey(pair.getPublic()));
            String privateKeyPEM = PemUtil.toPEMFormatPrivate(encodeKey(pair.getPrivate()));
            saveWalletsKeys(privateKeyPath, privateKeyPEM, publicKeyPath, publicKeyPEM);

            // Crear y guardar con Builder
            WalletKey newWalletKey = WalletKey.builder()
                    .wallet(wallet)
                    .publicKey(publicKeyPEM)
                    .privateKey(privateKeyPEM)
                    .build();

            return walletKeyRepository.save(newWalletKey);

        });
        // Verificar si ya existen claves
        String absolutePath = Paths.get(KEYS_FOLDER, "wallet_" + wallet.getId() + "_public.pem").toString();
        log.info("Claves guardadas en: {}", absolutePath);
        return WalletGenerateKeysDTO.generateKey(responseDb.getPublicKey(), absolutePath,wallet.getId());
    }

    private static KeyPairGenerator getKeyPairGenerator() {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return keyGen;
    }

    // Método para obtener la clave pública de una wallet (en formato PublicKey)
    public PublicKey getPublicKeyForWallet(Long walletId) {
        Optional<WalletKey> keyOpt = walletKeyRepository.findByWalletId(walletId);
        if (keyOpt.isPresent()) {
            String publicKeyPEM = keyOpt.get().getPublicKey();
            try {
                // Elimina encabezados, pies y saltos de línea
                String publicKeyContent = publicKeyPEM
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");
                byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePublic(keySpec);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Devuelve la clave privada asociada a la wallet.
     */
    public PrivateKey getPrivateKeyForWallet(Long walletId) {
        Optional<WalletKey> keyOpt = walletKeyRepository.findByWalletId(walletId);
        if (keyOpt.isPresent()) {
            String privateKeyPEM = keyOpt.get().getPrivateKey();
            try {
                String privateKeyContent = privateKeyPEM
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s", "");
                byte[] decoded = Base64.getDecoder().decode(privateKeyContent);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePrivate(keySpec);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private static void saveWalletsKeys(Path privateKeyPath, String privateKeyPEM, Path publicKeyPath, String publicKeyPEM) {
        saveWalletKeyPem(privateKeyPath, privateKeyPEM);
        saveWalletKeyPem(publicKeyPath, publicKeyPEM);
    }

    private static void saveWalletKeyPem(Path privateKeyPath, String privateKeyPEM) {
        // Guardar en archivos
        try (FileOutputStream fos = new FileOutputStream(privateKeyPath.toFile())) {
            fos.write(privateKeyPEM.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getPublicKeyPath(Wallet wallet) {
        return Path.of(KEYS_FOLDER, "wallet_" + wallet.getId() + "_public.pem");
    }

    private static Path getPrivateKeyPath(Wallet wallet) {
        return Path.of(KEYS_FOLDER, "wallet_" + wallet.getId() + "_private.pem");
    }

}