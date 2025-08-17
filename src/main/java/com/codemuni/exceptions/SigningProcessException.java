package com.codemuni.exceptions;

public class SigningProcessException extends RuntimeException{

    public SigningProcessException(String message) {
        super(message);
    }

    public SigningProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
