package com.vertx.example.web.verticle;

import com.vertx.example.web.handler.CronHelloHandler;
import io.vertx.core.AbstractVerticle;

public class TaskVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        new CronHelloHandler().start(null);
    }

}
