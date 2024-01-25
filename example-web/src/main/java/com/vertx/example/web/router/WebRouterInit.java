package com.vertx.example.web.router;

import cn.hutool.core.lang.Singleton;
import com.vertx.eventbus.bus.DemoBus;
import io.vertx.ext.web.Router;
import webserver.helper.RouterInitializer;

import static com.vertx.common.core.config.VertxLoadConfig.vertx;
import static com.vertx.common.core.entity.web.ApiResponse.successResponse;

public class WebRouterInit implements RouterInitializer {

  private final DemoBus demoBus = Singleton.get(DemoBus.class);

  /**
   * 初始化方法，用于设置路由
   *
   * @param router 路由器对象
   */
  public void init(Router router) {
    final Router mountRouter = mountRouter();
    router.route("/api/*").subRouter(mountRouter);
  }

  private Router mountRouter() {
    // 创建一个路由器对象
    final Router subRouter = Router.router(vertx);

    // 定义一个路由，当收到GET请求时处理
    subRouter.get("/test").handler(routingContext -> {
      final String res = demoBus.call("world!");
      // 向响应对象发送成功响应，并附带消息体为"test"
      routingContext.response().end(successResponse(res));
    });

    // 返回子路由器对象
    return subRouter;
  }
}
