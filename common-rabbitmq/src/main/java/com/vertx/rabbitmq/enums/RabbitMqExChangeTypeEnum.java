package com.vertx.rabbitmq.enums;

public enum RabbitMqExChangeTypeEnum {
  /**
   * 默认交换机
   */
  DEFAULT,

  /**
   * 直连交换机
   */
  DIRECT,

  /**
   * 主题交换机
   */
  TOPIC,

  /**
   * 头交换机
   */
  HEADERS,

  /**
   * 扇形交换机
   */
  FANOUT
}