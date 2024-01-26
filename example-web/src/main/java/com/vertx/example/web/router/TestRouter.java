package com.vertx.example.web.router;

import static com.vertx.common.core.config.VertxLoadConfig.vertx;
import static com.vertx.common.core.entity.web.ApiResponse.successResponse;

import com.vertx.eventbus.bus.DemoBus;

import cn.hutool.core.lang.Singleton;
import io.vertx.ext.web.Router;

public class TestRouter {

  private final DemoBus demoBus = Singleton.get(DemoBus.class);

  public void init(Router router) {
    final Router subRouter = Router.router(vertx);
    // 定义一个路由，当收到GET请求时处理
    subRouter.get("/hello").handler(routingContext -> {
      final String res = demoBus.call("world!");
      // 向响应对象发送成功响应，并附带消息体为"test"
      routingContext.response().end(successResponse(res));
    });
    router.route("/test/*").subRouter(subRouter);
  }
}
