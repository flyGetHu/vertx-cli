package com.vertx.example.web.verticle;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.config.VertxLoadConfig;
import com.vertx.mysql.client.MysqlClient;
import com.vertx.rabbitmq.client.RabbitMqClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;

import static io.vertx.core.Future.await;

public class MainVerticle extends AbstractVerticle {


    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        final long start = System.currentTimeMillis();
        try {
            new VertxLoadConfig().init("");
            // 初始化数据库连接
            new MysqlClient().init(VertxLoadConfig.appConfig.getDatabase().getMysql(), true);
            new RabbitMqClient().init(VertxLoadConfig.appConfig.getMq().getRabbitmq(), true);
            // 部署verticle配置
            final DeploymentOptions deploymentOptions = new DeploymentOptions();
            deploymentOptions.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
            // 获取可使用逻辑核心
            deploymentOptions.setInstances(1);
            await(vertx.deployVerticle(WebVerticle.class.getName(), deploymentOptions));
            await(vertx.deployVerticle(BusVerticle.class.getName(), deploymentOptions));
            await(vertx.deployVerticle(RabbitMqVerticle.class.getName(), deploymentOptions));
            StaticLog.info("部署成功");
            StaticLog.info("启动成功,耗时{}ms", System.currentTimeMillis() - start);
        } catch (Exception e) {
            StaticLog.error(e, "启动失败");
            startPromise.fail(e);
        }
    }
}
