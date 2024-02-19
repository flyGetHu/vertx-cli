package com.vertx.common.core.enums;

public interface ISharedLockSharedLockEnum {
    // 键 如果是多个参数，使用{}包裹，如：key = "user_{}_{}" args = ["1", "2"]
    String getKey();
    void setKey(String key);
}
