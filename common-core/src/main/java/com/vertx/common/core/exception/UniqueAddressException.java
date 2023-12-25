package com.vertx.common.core.exception;

public class UniqueAddressException extends RuntimeException {
    public UniqueAddressException(String msg) {
        super(msg);
    }

    public UniqueAddressException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
