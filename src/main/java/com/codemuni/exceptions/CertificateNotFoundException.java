package com.codemuni.exceptions;

public class CertificateNotFoundException extends RuntimeException {

    public CertificateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }


    public CertificateNotFoundException(String message) {
        super(message);
    }
}