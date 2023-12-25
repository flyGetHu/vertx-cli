package com.vertx.example.web.verticle;

import com.vertx.example.web.router.WebRouterInit;
import io.vertx.core.AbstractVerticle;
import webserver.entity.WebServiceOptions;
import webserver.helper.VertxWebHelper;

public class WebVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        final WebServiceOptions webServiceOptions = new WebServiceOptions();
        webServiceOptions.initRouter = new WebRouterInit();
        new VertxWebHelper().startHttpServer(webServiceOptions);
    }
}
