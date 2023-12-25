package com.vertx.example.web.verticle;

import com.vertx.example.web.router.WebRouterInit;
import io.vertx.core.AbstractVerticle;
import webserver.entity.WebServiceOptions;
import webserver.helper.VertxWebHelper;

public class WebVerticle extends AbstractVerticle {

    /**
     * 启动方法
     * @throws Exception 异常
     */
    @Override
    public void start() throws Exception {
        // 创建WebServiceOptions对象
        final WebServiceOptions webServiceOptions = new WebServiceOptions();

        // 初始化WebRouter
        webServiceOptions.initRouter = new WebRouterInit();

        // 启动HTTP服务器
        new VertxWebHelper().startHttpServer(webServiceOptions);
    }

}
