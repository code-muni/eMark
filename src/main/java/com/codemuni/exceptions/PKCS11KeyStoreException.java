package com.codemuni.exceptions;

public class PKCS11KeyStoreException extends RuntimeException {
    public PKCS11KeyStoreException(String message) {
        super(message);
    }
    public PKCS11KeyStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
