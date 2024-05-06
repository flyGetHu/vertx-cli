package com.vertx.rabbitmq.handler;

import com.vertx.common.core.entity.mq.MqMessageData;
import com.vertx.common.core.enums.IModelEnum;
import com.vertx.rabbitmq.enums.IRabbitMqExChangeEnum;

/**
 * Rabbit队列处理器接口
 * 此接口用于定义RabbitMQ队列的基本信息
 *
 * @param <Request> 请求对象类型
 */
public interface RabbitMqHandler<Request> {
    // 队列消息实体类型
    Class<Request> getRequestClass();

    // MQ队列交换机枚举，见RabbitMqExChangeEnum
    IRabbitMqExChangeEnum getExchange();

    // 模块名称，见ModelEnum
    IModelEnum getModuleName();

    // 队列名称 命名方式：以业务操作命名，见名知意，如：（注册用户）register.user
    // 全小写，单词之间用.分割
    String getQueueName();

    // 业务开始日期 命名方式：yyyy-MM-dd，如：2023-08-03
    String getDate();


    // 是否持久化，默认true，若为false，则重启服务后队列消失
    default boolean isDurable() {
        return true;
    }

    // 是否排他，默认false，若为true，则其他用户无法访问此队列
    default boolean isExclusive() {
        return false;
    }



    // 最大内部队列大小 默认100 0为不限制，超过此大小，则不再接收消息
    default int getMaxInternalQueueSize() {
        return 100;
    }


    // 是否自动ack，默认false，若为true，则消费消息后，自动确认
    default boolean isAutoAck() {
        return false;
    }


    // 最大重试次数，默认3次，超过此次数，则不再重试
    default int getMaxRetry() {
        return 3;
    }


    // 重试间隔时间，默认1000毫秒，单位毫秒
    default long getRetryInterval() {
        return 1000;
    }


    // 消息消费处理器，用于处理消费消息的业务逻辑
    String handle(Request request);

    // 消息持久化策略，消息发送前会执行此逻辑，可以将消息保存到数据库或者redis中，用于消息重试
    default String persist(MqMessageData<Request> message) {
        return null;
    }

    // 消息消费完成后回调函数，用于通知生产者消息是否消费成功，或者更新消息状态
    default void callback(String msg, String msgId) {

    }
}
