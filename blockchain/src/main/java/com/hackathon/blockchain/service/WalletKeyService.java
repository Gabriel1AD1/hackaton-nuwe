package com.hackathon.blockchain.service;

import com.hackathon.blockchain.dto.WalletGenerateKeysDTO;

public interface WalletKeyService {
    WalletGenerateKeysDTO generateAndStoreKeys(Long userId);
}
