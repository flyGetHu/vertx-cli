package com.vertx.rabbitmq.enums;

public enum RabbitMqExChangeEnum implements IRabbitMqExChangeEnum {
  // 默认交换机
  DEFAULT(RabbitMqExChangeTypeEnum.DEFAULT, "", String.class, true, false),
  TESTRabbitMqExChangeEnum(RabbitMqExChangeTypeEnum.FANOUT, "test", String.class, true, false),
  ;

  private final RabbitMqExChangeTypeEnum type;

  private final String exchanger;
  private final Class<?> messageType;
  private final boolean durable;
  private final boolean autoDelete;

  RabbitMqExChangeEnum(RabbitMqExChangeTypeEnum type, String exchanger, Class<?> messageType, boolean durable,
      boolean autoDelete) {
    this.type = type;
    this.exchanger = exchanger;
    this.messageType = messageType;
    this.durable = durable;
    this.autoDelete = autoDelete;
  }

  @Override
  public RabbitMqExChangeTypeEnum getType() {
    return type;
  }

  @Override
  public String getExchanger() {
    return exchanger;
  }

  @Override
  public Class<?> getMessageType() {
    return messageType;
  }

  @Override
  public boolean isDurable() {
    return durable;
  }

  @Override
  public boolean isAutoDelete() {
    return autoDelete;
  }
}