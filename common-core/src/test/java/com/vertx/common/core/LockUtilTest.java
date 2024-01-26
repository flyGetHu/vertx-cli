package com.vertx.common.core;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.vertx.common.core.config.VertxLoadConfig;
import com.vertx.common.core.utils.LockUtil;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.core.Vertx;

@ExtendWith(VertxExtension.class)
class LockUtilTest {

  @BeforeEach
  void deployVerticle(Vertx vertx, VertxTestContext context) {
    VertxLoadConfig.vertx = vertx;
    VertxLoadConfig.sharedData = vertx.sharedData();
  }

  @Test
  void testTryLock(VertxTestContext context) throws InterruptedException {
  }
}