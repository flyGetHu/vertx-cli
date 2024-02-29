package com.vertx.redis.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.app.AppConfig;
import com.vertx.redis.exception.RedisInitException;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;

import static com.vertx.common.core.config.VertxLoadConfig.isInit;
import static com.vertx.common.core.config.VertxLoadConfig.vertx;
import static io.vertx.core.Future.await;

public class RedisClient {

    public static RedisAPI redisClient;

    public static RedisAPI init(AppConfig.Redis config, Boolean isDefault) {
        if (!isInit) {
            throw new RedisInitException("Redis init error");
        }
        final RedisOptions options = new RedisOptions();
        final String host = config.getHost();
        if (StrUtil.isBlank(host)) {
            throw new RedisInitException("Redis host is null");
        }
        final int port = config.getPort();
        if (port <= 0) {
            throw new RedisInitException("Redis port is null");
        }
        final int database = config.getDatabase();
        if (database <= 0) {
            throw new RedisInitException("Redis database is null");
        }
        final String password = config.getPassword();
        if (StrUtil.isNotBlank(password)) {
            options.setPassword(password);
        }
        options.setMaxPoolSize(config.getMaxPoolSize());
        options.setMaxPoolWaiting(config.getMaxWaitQueueSize());
        final String url = StrUtil.format("redis://{}:{}/{}", host, port, database);
        options.setConnectionString(url);
        final RedisConnection redisConnection = await(Redis.createClient(vertx, options).connect());
        redisConnection.exceptionHandler(event -> {
            StaticLog.error(event.getCause(), "redis链接断开");
            attemptReconnect(0, config);
        });
        final RedisAPI api = RedisAPI.api(redisConnection);
        if (isDefault) {
            redisClient = api;
        } else {
            return api;
        }
        StaticLog.info("redisClient链接成功");
        return null;
    }

    /**
     * Attempt to reconnect up to MAX_RECONNECT_RETRIES
     */
    private static void attemptReconnect(int retry, AppConfig.Redis config) {
        if (retry > 60) {
            StaticLog.error("redisClient链接断开,重试次数超过60次，不再重试");
        } else {
            // retry with backoff up to 10240 ms
            vertx.setTimer(1000, event -> {
                try {
                    init(config, true);
                } catch (Throwable e) {
                    StaticLog.error(e, "redisClient链接断开,重试第${retry + 1}次");
                    attemptReconnect(retry + 1, config);
                }
            });
        }
    }
}
