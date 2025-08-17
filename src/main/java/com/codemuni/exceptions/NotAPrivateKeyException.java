package com.codemuni.exceptions;

public class NotAPrivateKeyException extends Exception {
  public NotAPrivateKeyException(String message) {
    super(message);
  }
  public NotAPrivateKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}