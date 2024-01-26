package com.vertx.common.core;

import com.vertx.common.core.config.VertxLoadConfig;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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