package com.vertx.rabbitmq.enums;

public interface IRabbitMqExChangeEnum {
  // 交换机类型
  RabbitMqExChangeTypeEnum getType();

  // 交换机名称
  String getExchanger();

  // 消息类型
  Object getMessageType();

  // 是否持久化
  boolean isDurable();

  // 是否自动删除
  boolean isAutoDelete();
}