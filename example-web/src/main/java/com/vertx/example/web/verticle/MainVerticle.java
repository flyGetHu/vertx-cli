package com.vertx.example.web.verticle;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.config.VertxLoadConfig;
import com.vertx.mysql.client.MysqlClient;
import com.vertx.rabbitmq.client.RabbitMqClient;
import io.vertx.core.*;

import java.util.List;

public class MainVerticle extends AbstractVerticle {


    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        final long start = System.currentTimeMillis();
        try {
            new VertxLoadConfig().init("huan");
            // 初始化数据库连接
            new MysqlClient().init(VertxLoadConfig.appConfig.getDatabase().getMysql(), true);
            new RabbitMqClient().init(VertxLoadConfig.appConfig.getMq().getRabbitmq(), true);
            // 部署verticle配置
            final DeploymentOptions deploymentOptions = new DeploymentOptions();
            deploymentOptions.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
            // 获取可使用逻辑核心
            deploymentOptions.setInstances(16);
            final List<Future<String>> futures = List.of(
                    vertx.deployVerticle(WebVerticle.class.getName(), deploymentOptions),
                    vertx.deployVerticle(BusVerticle.class.getName(), deploymentOptions),
                    vertx.deployVerticle(RabbitMqVerticle.class.getName(), deploymentOptions));
            Future.all(futures)
                    .onSuccess(aVoid -> {
                        StaticLog.info("部署成功");
                        StaticLog.info("启动成功,耗时{}ms", System.currentTimeMillis() - start);
                        startPromise.complete();
                    })
                    .onFailure(throwable -> StaticLog.error(throwable, "部署失败"));
        } catch (Exception e) {
            StaticLog.error(e, "启动失败");
            startPromise.fail(e);
        }
    }
}
