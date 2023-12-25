package com.vertx.example.web.router;

import io.vertx.ext.web.Router;
import webserver.helper.RouterInitializer;

import static com.vertx.common.core.entity.web.ApiResponse.successResponse;

public class WebRouterInit implements RouterInitializer {

    public void init(Router router) {
        router.get("/api/test").handler(routingContext -> {
            routingContext.response().end(successResponse("test"));
        });
    }
}
