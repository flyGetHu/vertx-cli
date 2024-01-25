package com.vertx.common.core.enums;

public enum ModelEnum implements IModelEnum {
  TEST_MODEL("test", "测试模块");

  private String modelName;
  private String description;

  ModelEnum(String modelName, String description) {
    this.modelName = modelName;
    this.description = description;
  }

  @Override
  public String getModelName() {
    return modelName;
  }

  @Override
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }
}