package com.vertx.common.core.exception;

/**
 * 尝试获取锁异常
 */
public class TryLockException extends RuntimeException {
  public TryLockException(String message, Throwable cause) {
    super(message, cause);
  }

  public TryLockException(String string) {
    super(string);
  }
}
