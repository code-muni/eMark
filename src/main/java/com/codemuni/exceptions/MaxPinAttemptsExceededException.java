package com.codemuni.exceptions;

/**
 * Exception thrown when the maximum number of PIN entry attempts is exceeded.
 */
public class MaxPinAttemptsExceededException extends Exception {
    public MaxPinAttemptsExceededException(String message) {
        super(message);
    }

    public MaxPinAttemptsExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}