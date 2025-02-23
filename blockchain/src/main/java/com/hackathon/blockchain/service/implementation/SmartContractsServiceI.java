package com.hackathon.blockchain.service.implementation;

import com.hackathon.blockchain.dto.ResponseDTO;
import com.hackathon.blockchain.dto.SmartContractRequestDTO;
import com.hackathon.blockchain.dto.SmartContractResponseDTO;
import com.hackathon.blockchain.enums.SmartContractStatus;
import com.hackathon.blockchain.exception.EntityNotFoundException;
import com.hackathon.blockchain.model.SmartContract;
import com.hackathon.blockchain.model.Wallet;
import com.hackathon.blockchain.model.WalletKey;
import com.hackathon.blockchain.repository.SmartContractRepository;
import com.hackathon.blockchain.repository.UserRepository;
import com.hackathon.blockchain.repository.WalletKeyRepository;
import com.hackathon.blockchain.repository.WalletRepository;
import com.hackathon.blockchain.service.SmartContractsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SmartContractsServiceI implements SmartContractsService
{
    private final SmartContractRepository smartContractRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletKeyRepository walletKeyRepository;
    @Override
    public SmartContractResponseDTO newContract(SmartContractRequestDTO dto, Long userId) {
        // Obtener el usuario y la wallet
        userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );

        Wallet walletDb = walletRepository.findByUserId(userId).orElseThrow(
                () -> new EntityNotFoundException("Wallet not found")
        );

        // Crear el contrato inteligente a partir del DTO
        SmartContract smartContract = SmartContract.builder()
                .name(dto.getName())
                .conditionExpression(dto.getConditionExpression())
                .action(dto.getAction()) // Convertir a enum
                .actionValue(dto.getActionValue())
                .issuerWalletId(walletDb.getId()) // Usar el ID de la wallet recuperada
                .status(SmartContractStatus.ACTIVE)
                .digitalSignature("") // Inicialmente vacío, se firmará más adelante
                .build();

        // Firmar el contrato usando la clave privada de la wallet
        String digitalSignature = signContract(smartContract, walletDb);
        smartContract.setDigitalSignature(digitalSignature);

        // Guardar el contrato inteligente en la base de datos
        SmartContract savedContract = smartContractRepository.save(smartContract);

        // Construir el DTO de respuesta
        return responseDTO(savedContract);
    }
    private String signContract(SmartContract smartContract, Wallet wallet) {
        Optional<PrivateKey> privateKey = Optional.ofNullable(getPrivateKeyForWallet(wallet.getId()));
        if (privateKey.isEmpty()) {
            throw new RuntimeException("No se pudo obtener la clave privada de la wallet");
        }

        try {
            // Convertir el contrato a un formato adecuado para la firma (por ejemplo, un JSON o un hash)
            String dataToSign = smartContract.toString(); // O convertir a JSON
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey.get());
            signature.update(dataToSign.getBytes());

            // Generar la firma
            byte[] signedData = signature.sign();
            return Base64.getEncoder().encodeToString(signedData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al firmar el contrato", e);
        }
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

    private static SmartContractResponseDTO responseDTO(SmartContract savedContract) {
        return SmartContractResponseDTO.builder()
                .id(savedContract.getId())
                .name(savedContract.getName())
                .conditionExpression(savedContract.getConditionExpression())
                .action(savedContract.getAction().name()) // Obtener el nombre del enum
                .actionValue(savedContract.getActionValue())
                .issuerWalletId(savedContract.getIssuerWalletId())
                .digitalSignature(savedContract.getDigitalSignature())
                .build();
    }

    @Override
    public ResponseDTO isValid(Long smartContractId) {
        SmartContract smartContractDb = smartContractRepository.findById(smartContractId).orElseThrow(
                () -> new EntityNotFoundException("SmartContract not found")
        );
        return ResponseDTO.smartContractValidMessage();
    }
}
