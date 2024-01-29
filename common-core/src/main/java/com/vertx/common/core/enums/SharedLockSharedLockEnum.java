package com.vertx.common.core.enums;

import lombok.Getter;

@Getter
public enum SharedLockSharedLockEnum  implements ISharedLockSharedLockEnum {
    USER("user_{}");

    private String key;

    SharedLockSharedLockEnum(String key) {
        this.key = key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }
}
