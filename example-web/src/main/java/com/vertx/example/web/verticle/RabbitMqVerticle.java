package com.vertx.example.web.verticle;

import com.vertx.example.web.handler.TestCustomerHandlerImpl;
import com.vertx.rabbitmq.helper.RabbitMqHelper;
import io.vertx.core.AbstractVerticle;

public class RabbitMqVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        RabbitMqHelper.registerConsumer(new TestCustomerHandlerImpl());
    }
}
