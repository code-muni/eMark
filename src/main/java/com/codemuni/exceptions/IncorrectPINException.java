package com.codemuni.exceptions;

public class IncorrectPINException extends Exception {
    public IncorrectPINException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectPINException(String message) {
        super(message);
    }
}