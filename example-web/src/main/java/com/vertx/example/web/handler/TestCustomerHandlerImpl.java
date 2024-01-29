package com.vertx.example.web.handler;

import cn.hutool.log.StaticLog;
import com.vertx.example.web.model.User;
import io.vertx.core.json.Json;

public class TestCustomerHandlerImpl extends TestCustomerHandler {

    @Override
    public String handle(User user) {
        StaticLog.info("TestCustomerHandlerImpl handle: {}", Json.encode(user));
        return "error";
    }
}
