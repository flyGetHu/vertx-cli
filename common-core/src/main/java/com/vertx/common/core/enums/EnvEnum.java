package com.vertx.common.core.enums;

import lombok.Getter;

@Getter
public enum EnvEnum {
    DEV("dev"),
    TEST("test"),
    PROD("prod");

    private final String value;

    EnvEnum(String value) {
        this.value = value;
    }
}
