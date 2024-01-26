package com.vertx.rabbitmq.handler;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.annotations.UniqueAddress;
import com.vertx.common.core.entity.mq.MqMessageData;
import com.vertx.common.core.enums.ModelEnum;
import com.vertx.rabbitmq.enums.RabbitMqExChangeEnum;

@UniqueAddress("dev.test.test.2023-08-03.test.fanout")
public class TestCustomerHandler implements RabbitMqHandler<String> {
    private Class<String> requestClass = String.class;
    private RabbitMqExChangeEnum exchange = RabbitMqExChangeEnum.TESTRabbitMqExChangeEnum;
    private ModelEnum moduleName = ModelEnum.TEST_MODEL;

    private String queueName = "test";
    private String date = "2023-08-03";
    private boolean durable = true;
    private boolean exclusive = false;
    private int maxInternalQueueSize = 100;
    private boolean autoAck = false;
    private int maxRetry = 3;
    private long retryInterval = 1000;

    @Override
    public Class<String> getRequestClass() {
        return requestClass;
    }

    @Override
    public RabbitMqExChangeEnum getExchange() {
        return exchange;
    }

    @Override
    public ModelEnum getModuleName() {
        return moduleName;
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean isDurable() {
        return durable;
    }

    @Override
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    @Override
    public boolean isExclusive() {
        return exclusive;
    }

    @Override
    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    @Override
    public int getMaxInternalQueueSize() {
        return maxInternalQueueSize;
    }

    @Override
    public void setMaxInternalQueueSize(int size) {
        this.maxInternalQueueSize = size;
    }

    @Override
    public boolean isAutoAck() {
        return autoAck;
    }

    @Override
    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }

    @Override
    public int getMaxRetry() {
        return maxRetry;
    }

    @Override
    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @Override
    public long getRetryInterval() {
        return retryInterval;
    }

    @Override
    public void setRetryInterval(long interval) {
        this.retryInterval = interval;
    }

    @Override
    public String handle(String request) {
        return null;
    }

    @Override
    public String persist(MqMessageData<String> message) {
        StaticLog.info("持久化函数执行成功");
        return null;
    }

    @Override
    public void callback(String msg, String msgId) {
    }
}