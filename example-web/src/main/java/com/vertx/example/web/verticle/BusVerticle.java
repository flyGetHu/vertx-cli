package com.vertx.example.web.verticle;

import com.vertx.eventbus.bus.DemoBus;
import io.vertx.core.AbstractVerticle;

public class BusVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        new DemoBus().register();
    }
}
