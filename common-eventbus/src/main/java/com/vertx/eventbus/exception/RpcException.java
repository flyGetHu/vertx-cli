package com.vertx.eventbus.exception;

/**
 * RPC异常
 */
public class RpcException extends RuntimeException {
  public RpcException(String message, Throwable cause) {
    super(message, cause);
  }
}
