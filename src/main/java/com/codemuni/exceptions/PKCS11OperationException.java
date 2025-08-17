package com.codemuni.exceptions;

public class PKCS11OperationException extends RuntimeException {
    public PKCS11OperationException(String message) {
        super(message);
    }
    public PKCS11OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
