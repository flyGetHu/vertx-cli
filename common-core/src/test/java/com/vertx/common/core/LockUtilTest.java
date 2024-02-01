package com.vertx.common.core;

import com.vertx.common.core.config.VertxLoadConfig;
import com.vertx.common.core.utils.LockUtil;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Supplier;

@ExtendWith(VertxExtension.class)
class LockUtilTest {

    @BeforeEach
    void deployVerticle(Vertx vertx, VertxTestContext context) {
        VertxLoadConfig.vertx = vertx;
        VertxLoadConfig.sharedData = vertx.sharedData();
    }

    @Test
    void testTryLock(VertxTestContext context) throws InterruptedException {
        final Object test = LockUtil.withLock("test", () -> {
            context.completeNow();
            return 2;
        });
        final String test2 = LockUtil.withLock("test2", () -> "test2");
        System.out.println(test);
        System.out.println(test2);
        context.completeNow();
    }
}