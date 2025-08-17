package com.codemuni.exceptions;

public class KeyStorePinException extends RuntimeException {
    public KeyStorePinException(String message) {
        super(message);
    }
    public KeyStorePinException(String message, Throwable cause) {
        super(message, cause);
    }
}
