package com.hackathon.blockchain.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class EncodeUtils {
    public static String encodeKey(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String encodeKey(PrivateKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
