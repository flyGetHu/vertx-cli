package com.vertx.example.web;

import cn.hutool.log.StaticLog;
import com.hazelcast.config.*;
import com.vertx.example.web.verticle.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class ExampleWebApp {

    /**
     * 主方法
     * v
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 加载配置文件
        final Config config = ConfigUtil.loadConfig();
        // 获取网络配置
        final NetworkConfig networkConfig = config.getNetworkConfig();
        // 获取加入配置
        final JoinConfig join = networkConfig.getJoin();
        // 获取多播配置
        final MulticastConfig multicastConfig = join.getMulticastConfig();
        // 禁用多播
        multicastConfig.setEnabled(false);
        // 获取TCP/IP配置
        final TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
        // 启用TCP/IP
        tcpIpConfig.setEnabled(true);
        // 添加成员"127.0.0.1"
        tcpIpConfig.addMember("127.0.0.1");
        // 创建Hazelcast集群管理器
        final HazelcastClusterManager hazelcastClusterManager = new HazelcastClusterManager(config);
        // 集群Vertx
        Vertx.builder().withClusterManager(hazelcastClusterManager).buildClustered()
                .onSuccess(res -> {
                    // 设置系统属性
                    System.setProperty("org.jooq.no-logo", "true");
                    System.setProperty("org.jooq.no-tips", "true");
                    // 创建部署选项
                    final DeploymentOptions deploymentOptions = new DeploymentOptions();
                    deploymentOptions.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
                    // 部署Verticle
                    res.deployVerticle(MainVerticle.class.getName(), deploymentOptions);
                    // 日志信息：应用启动成功
                    StaticLog.info("App start success");
                }).onFailure(e -> {
                    // 日志错误信息：应用启动失败
                    StaticLog.error("App start fail", e);
                });
    }
}
