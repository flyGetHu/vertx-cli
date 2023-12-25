package com.vertx.example.web.verticle;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.config.VertxLoadConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;

import static io.vertx.core.Future.await;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        new VertxLoadConfig().init("");
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
        vertx.deployVerticle(WebVerticle.class.getName(), deploymentOptions);
        StaticLog.info("启动项目成功");
    }
}
