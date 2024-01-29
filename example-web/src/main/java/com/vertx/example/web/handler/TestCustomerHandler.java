package com.vertx.example.web.handler;

import com.vertx.common.core.annotations.UniqueAddress;
import com.vertx.common.core.entity.mq.MqMessageData;
import com.vertx.common.core.enums.ModelEnum;
import com.vertx.example.web.model.User;
import com.vertx.example.web.enums.RabbitMqExChangeEnum;
import com.vertx.rabbitmq.handler.RabbitMqHandler;
import lombok.Data;

@Data
@UniqueAddress("dev.test.test.2023-08-03.test.fanout")
public class TestCustomerHandler implements RabbitMqHandler<User> {
    private Class<User> requestClass = User.class;
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
    public String handle(User user) {
        return null;
    }

    @Override
    public String persist(MqMessageData<User> message) {
        return null;
    }

    @Override
    public void callback(String msg, String msgId) {

    }
}