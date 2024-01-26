package com.vertx.redis.exception;

/**
 * 针对Redis初始化错误的自定义异常类。
 * 扩展标准异常类。
 */
public class RedisInitException extends RuntimeException {
    public RedisInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisInitException(String message) {
        super(message);
    }
}
