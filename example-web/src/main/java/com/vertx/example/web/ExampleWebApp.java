package com.vertx.example.web;

import cn.hutool.log.StaticLog;
import com.hazelcast.config.*;
import com.vertx.example.web.verticle.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.VertxBuilder;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class ExampleWebApp {

    public static void main(String[] args) {
        final Config config = ConfigUtil.loadConfig();
        final VertxOptions options = getVertxOptions(config);
        Vertx.clusteredVertx(options).onSuccess(res -> {
            System.setProperty("org.jooq.no-logo", "true");
            System.setProperty("org.jooq.no-tips", "true");
            final DeploymentOptions deploymentOptions = new DeploymentOptions();
            deploymentOptions.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
            res.deployVerticle(MainVerticle.class.getName(), deploymentOptions);
            StaticLog.info("App start success");
        }).onFailure(e -> {
            StaticLog.error("App start fail", e);
        });
    }

    private static VertxOptions getVertxOptions(Config config) {
        final NetworkConfig networkConfig = config.getNetworkConfig();
        final JoinConfig join = networkConfig.getJoin();
        final MulticastConfig multicastConfig = join.getMulticastConfig();
        multicastConfig.setEnabled(false);
        final TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.addMember("127.0.0.1");
        final HazelcastClusterManager hazelcastClusterManager = new HazelcastClusterManager(config);
        final VertxBuilder vertxBuilder = new VertxBuilder();
        vertxBuilder.clusterManager(hazelcastClusterManager);
        return vertxBuilder.options();
    }
}
