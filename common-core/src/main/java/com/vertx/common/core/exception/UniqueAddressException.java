package com.vertx.common.core.exception;

/**
 * UniqueAddressException异常类，继承自RuntimeException。
 * 当出现唯一地址问题时，会抛出此异常。
 *
 * @param msg   异常消息
 * @param cause 异常的根源，如果知道的话
 */
public class UniqueAddressException extends RuntimeException {
  /**
   * 构造一个新的UniqueAddressException异常，使用给定的消息。
   *
   * @param msg 异常消息
   */
  public UniqueAddressException(String msg) {
    super(msg);
  }

  /**
   * 构造一个新的UniqueAddressException异常，使用给定的消息和根源。
   *
   * @param msg   异常消息
   * @param cause 异常的根源
   */
  public UniqueAddressException(String msg, Throwable cause) {
    super(msg, cause);
  }
}