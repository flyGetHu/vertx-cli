package com.vertx.rabbitmq.exception;

public class RabbitMQSendException extends RuntimeException {

    public RabbitMQSendException(String message) {
        super(message);
    }

    public RabbitMQSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public RabbitMQSendException(Throwable cause) {
        super(cause);
    }

    public RabbitMQSendException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
