package com.codemuni.exceptions;

public class SignatureImageException extends RuntimeException {
    public SignatureImageException(String message) {
        super(message);
    }
    public SignatureImageException(String message, Throwable cause) {
        super(message, cause);
    }
}
