package com.vertx.rabbitmq.client;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.app.AppConfig;
import com.vertx.common.core.utils.StrUtil;
import com.vertx.rabbitmq.exception.RabbitMqClientInitException;

import io.vertx.core.Future;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

import static com.vertx.common.core.config.VertxLoadConfig.isInit;
import static com.vertx.common.core.config.VertxLoadConfig.vertx;

public class RabbitMqClient {

  public static RabbitMQClient rabbitMqClient;

  /**
   * 初始化连接池
   *
   * @param config RabbitMQ连接配置
   * @param isDef  是否为默认连接池
   * @throws RabbitMqClientInitException 如果全局初始化未完成，则抛出该异常
   */
  public RabbitMQClient init(AppConfig.Rabbitmq config, Boolean isDef) {
    if (!isInit) {
      StaticLog.error("全局初始化未完成,请先调用:VertxLoadConfig.init()");
      throw new RabbitMqClientInitException("全局初始化未完成,请先调用:VertxLoadConfig.init()");
    }
    final RabbitMQOptions rabbitMQOptions = new RabbitMQOptions();
    final String host = config.getHost();
    if (StrUtil.isBlank(host)) {
      StaticLog.error("rabbitmq host is null");
      throw new RabbitMqClientInitException("rabbitmq host is null");
    }
    rabbitMQOptions.setHost(host);
    final int port = config.getPort();
    rabbitMQOptions.setPort(port);
    final String username = config.getUsername();
    if (StrUtil.isBlank(username)) {
      StaticLog.error("rabbitmq username is null");
      throw new RabbitMqClientInitException("rabbitmq username is null");
    }
    rabbitMQOptions.setUser(username);
    final String password = config.getPassword();
    if (StrUtil.isBlank(password)) {
      StaticLog.error("rabbitmq password is null");
      throw new RabbitMqClientInitException("rabbitmq password is null");
    }
    rabbitMQOptions.setPassword(password);
    final String virtualHost = config.getVirtualHost();
    if (StrUtil.isBlank(virtualHost)) {
      StaticLog.error("rabbitmq virtualHost is null");
      throw new RabbitMqClientInitException("rabbitmq virtualHost is null");
    }
    // 设置虚拟主机
    rabbitMQOptions.setVirtualHost(virtualHost);
    // 设置连接超时时间
    rabbitMQOptions.setConnectionTimeout(config.getConnectionTimeout());
    // 设置请求心跳时间
    rabbitMQOptions.setRequestedHeartbeat(config.getRequestedHeartbeat());
    // 设置心跳超时时间
    rabbitMQOptions.setHandshakeTimeout(config.getHandshakeTimeout());
    // 设置自动重连
    rabbitMQOptions.setAutomaticRecoveryEnabled(config.getAutomaticRecoveryEnabled());
    // 设置重连次数
    rabbitMQOptions.setReconnectAttempts(config.getReconnectAttempts());
    // 重连间隔时间
    rabbitMQOptions.setNetworkRecoveryInterval(config.getNetworkRecoveryInterval());
    // 请求通道最大数
    rabbitMQOptions.setRequestedChannelMax(config.getRequestedChannelMax());
    // 请求心跳超时时间
    rabbitMQOptions.setRequestedHeartbeat(config.getRequestedHeartbeat());

    final RabbitMQClient rabbitMQClient = RabbitMQClient.create(vertx, rabbitMQOptions);
    Future.await(rabbitMQClient.start());
    if (config.getSendConfirm()) {
      Future.await(rabbitMQClient.confirmSelect());
    }
    Future.await(rabbitMQClient.basicQos(config.getMaxQos()));
    if (isDef) {
      RabbitMqClient.rabbitMqClient = rabbitMQClient;
    } else {
      return rabbitMQClient;
    }
    return null;
  }
}
