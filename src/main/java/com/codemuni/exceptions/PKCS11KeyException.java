package com.codemuni.exceptions;

public class PKCS11KeyException extends RuntimeException {
    public PKCS11KeyException(String message) {
        super(message);
    }
    public PKCS11KeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
