package com.vertx.eventbus.bus;

import com.vertx.common.core.annotations.UniqueAddress;
import com.vertx.eventbus.handler.BusHandler;

@UniqueAddress("demo://eventbus")
public class DemoBus implements BusHandler<String, String> {
    @Override
    public Class<String> getRequestClass() {
        return String.class;
    }

    @Override
    public Class<String> getResponseClass() {
        return String.class;
    }

    @Override
    public String getAddress() {
        return "demo://eventbus";
    }

    @Override
    public String handle(String s) {
        return "Hello " + s;
    }
}
