package com.vertx.example.web.router;

import static com.vertx.common.core.config.VertxLoadConfig.vertx;

import io.vertx.ext.web.Router;
import webserver.helper.RouterInitializer;

public class WebRouterInit implements RouterInitializer {

  /**
   * 初始化方法，用于设置路由
   *
   * @param router 路由器对象
   */
  public void init(Router router) {
    final Router mountRouter = Router.router(vertx);
    // 挂载路由
    new TestRouter().init(mountRouter);
    new UserRouter().init(mountRouter);
    // 挂载到根路由
    router.route("/v1/*").subRouter(mountRouter);
  }
}
