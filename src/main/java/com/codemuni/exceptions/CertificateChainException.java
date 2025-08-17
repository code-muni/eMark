package com.codemuni.exceptions;

public class CertificateChainException extends RuntimeException {
    public CertificateChainException(String message) {
        super(message);
    }
    public CertificateChainException(String message, Throwable cause) {
        super(message, cause);
    }
}
