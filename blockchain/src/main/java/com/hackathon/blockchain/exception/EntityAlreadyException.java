package com.hackathon.blockchain.exception;

public class EntityAlreadyException extends RuntimeException {
    public EntityAlreadyException(String message) {
        super(message);
    }
}
