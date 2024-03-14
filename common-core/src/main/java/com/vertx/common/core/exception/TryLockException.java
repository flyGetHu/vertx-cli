package com.vertx.common.core.exception;

public class TryLockException extends RuntimeException {
  public TryLockException(String message, Throwable cause) {
    super(message, cause);
  }

  public TryLockException(String string) {
    super(string);
  }
}
