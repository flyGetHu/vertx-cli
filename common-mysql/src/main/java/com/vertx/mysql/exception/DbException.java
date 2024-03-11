package com.vertx.mysql.exception;

/**
 * 数据库操作异常
 */
public class DbException extends RuntimeException {
  public DbException(String message, Throwable cause) {
    super(message, cause);
  }

  public DbException(String message) {
    super(message);
  }
}