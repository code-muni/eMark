package com.codemuni.exceptions;

public class KeyStoreInitializationException extends RuntimeException {
    public KeyStoreInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
    public KeyStoreInitializationException(String message) {
        super(message);
    }
}