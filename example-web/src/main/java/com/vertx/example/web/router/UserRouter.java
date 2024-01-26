package com.vertx.example.web.router;

import cn.hutool.core.lang.Singleton;
import com.vertx.example.web.model.User;
import com.vertx.example.web.service.UserService;
import io.vertx.ext.web.Router;

import java.util.List;

import static com.vertx.common.core.config.VertxLoadConfig.vertx;
import static com.vertx.common.core.entity.web.ApiResponse.successResponse;

public class UserRouter {

    private final UserService userService = Singleton.get(UserService.class);

    public void init(Router router) {
        final Router subRouter = Router.router(vertx);
        subRouter.get("/list").handler(routingContext -> {
            List<User> users = userService.selectAll();
            routingContext.end(successResponse(users));
        });
        router.route("/user/*").subRouter(subRouter);
    }
}
