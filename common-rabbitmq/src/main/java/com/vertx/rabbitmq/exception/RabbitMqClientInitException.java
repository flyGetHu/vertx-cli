package com.vertx.rabbitmq.exception;

public class RabbitMqClientInitException extends RuntimeException {

    public RabbitMqClientInitException(String message) {
        super(message);
    }

    public RabbitMqClientInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public RabbitMqClientInitException(Throwable cause) {
        super(cause);
    }

    public RabbitMqClientInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
