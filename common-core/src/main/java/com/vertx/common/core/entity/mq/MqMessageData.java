package com.vertx.common.core.entity.mq;

import cn.hutool.core.lang.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息数据
 *
 * @param <T> 消息类型
 */
@Getter
public class MqMessageData<T> {
    // 消息
    @Setter
    @JsonProperty("body")
    private T body;

    // 消息id
    @JsonProperty("id")
    private String id;

    public MqMessageData(T body) {
        this.body = body;
        this.id = UUID.fastUUID().toString();
    }

}