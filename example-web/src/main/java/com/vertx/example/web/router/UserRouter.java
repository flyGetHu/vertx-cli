package com.vertx.example.web.router;

import cn.hutool.core.lang.Singleton;
import com.vertx.common.core.entity.db.QueryPageParam;
import com.vertx.common.core.entity.db.QueryPageResponse;
import com.vertx.example.web.entity.request.UserPageRequest;
import com.vertx.example.web.entity.request.condition.UserPageCondition;
import com.vertx.example.web.model.User;
import com.vertx.example.web.service.UserService;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;

import static com.vertx.common.core.config.VertxLoadConfig.vertx;
import static com.vertx.common.core.entity.web.ApiResponse.errorResponse;
import static com.vertx.common.core.entity.web.ApiResponse.successResponse;

public class UserRouter {

    private final UserService userService = Singleton.get(UserService.class);

    public void init(Router router) {
        final Router subRouter = Router.router(vertx);

        subRouter.get("/list").handler(routingContext -> {
            List<User> users = userService.selectAll();
            routingContext.end(successResponse(users));
        });

        subRouter.post("/page").handler(BodyHandler.create()).handler(routingContext -> {
            final RequestBody body = routingContext.body();
            final QueryPageParam<UserPageCondition> queryPageParam = body.asPojo(UserPageRequest.class);
            if (queryPageParam == null || queryPageParam.getCondition() == null) {
                routingContext.end(errorResponse("参数错误"));
                return;
            }
            final QueryPageResponse<User> userQueryPageResponse = userService.selectPage(queryPageParam);
            if (userQueryPageResponse == null) {
                routingContext.end(errorResponse("查询失败"));
                return;
            }
            routingContext.end(successResponse(userQueryPageResponse));
        });
        router.route("/user/*").subRouter(subRouter);
    }
}
