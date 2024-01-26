package com.vertx.common.core.entity.mq;

import cn.hutool.core.lang.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 消息数据
 *
 * @param <T> 消息类型
 */
public class MqMessageData<T> {
    // 消息
    @JsonProperty("body")
    private T body;

    // 消息id
    @JsonProperty("id")
    private String id;

    public MqMessageData(T body) {
        this.body = body;
        this.id = UUID.fastUUID().toString();
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }
}