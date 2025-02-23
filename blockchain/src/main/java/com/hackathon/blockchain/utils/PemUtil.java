package com.hackathon.blockchain.utils;

import java.security.PrivateKey;
import java.security.PublicKey;

public class PemUtil {
    public static String toPEMFormat(PublicKey aPublic, String aPublic1) {
        return "";
    }

    public static String toPEMFormat(PrivateKey aPrivate, String aPrivate1) {
        return "";
    }
    public static String toPEMFormatPrivate(String key) {
        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN PRIVATE KEY-----\n");

        // Asegúrate de que la clave esté dividida en líneas de 64 caracteres
        int lineLength = 64;
        for (int i = 0; i < key.length(); i += lineLength) {
            int end = Math.min(i + lineLength, key.length());
            pemBuilder.append(key, i, end).append("\n");
        }

        pemBuilder.append("-----END PRIVATE KEY-----\n");
        return pemBuilder.toString();
    }

    public static String toPEMFormatPublic(String key) {
        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN PUBLIC KEY-----\n");

        // Asegúrate de que la clave esté dividida en líneas de 64 caracteres
        int lineLength = 64;
        for (int i = 0; i < key.length(); i += lineLength) {
            int end = Math.min(i + lineLength, key.length());
            pemBuilder.append(key, i, end).append("\n");
        }

        pemBuilder.append("-----END PUBLIC KEY-----\n");
        return pemBuilder.toString();
    }


}
