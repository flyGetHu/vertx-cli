package com.vertx.mysql.exception;

/**
 * 数据库连接初始化异常
 */
public class MysqlClientInitException extends RuntimeException {
    public MysqlClientInitException(String message) {
        super(message);
    }

    public MysqlClientInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public MysqlClientInitException(Throwable cause) {
        super(cause);
    }
}
