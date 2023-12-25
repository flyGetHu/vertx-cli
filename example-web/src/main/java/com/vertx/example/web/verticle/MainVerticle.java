package com.vertx.example.web.verticle;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.config.VertxLoadConfig;
import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        try {
            new VertxLoadConfig().init("");
            StaticLog.info("启动项目成功");
        } catch (Exception e) {
            StaticLog.error(e, "启动项目失败");
        }
    }
}
