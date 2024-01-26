package com.vertx.common.core.utils;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.config.VertxLoadConfig;
import io.vertx.core.Future;
import io.vertx.core.shareddata.Lock;

import java.util.concurrent.TimeUnit;

/**
 * A utility class that provides local locking functionality.
 * <p>
 * This class is thread-safe.
 */
public class LockUtil {

    /**
     * 尝试获取指定键的锁。
     *
     * @param key     与锁关联的键
     * @param timeout 等待锁的最长时间 (秒)
     * @return 获得的锁，如果无法获得锁，则返回null
     */
    public static Lock tryLock(String key, long timeout) {
        return tryLock(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取指定键和超时的锁。
     *
     * @param key     与锁关联的键
     * @param timeout 等待锁的最长时间
     * @param unit    超时参数的时间单位
     * @return 获得的锁，如果在指定的超时内无法获得锁，则返回null
     */
    public static Lock tryLock(String key, long timeout, TimeUnit unit) {
        Lock lock = null;
        try {
            lock = Future.await(VertxLoadConfig.sharedData.getLockWithTimeout(key, unit.toMillis(timeout)));
        } catch (Exception e) {
            StaticLog.warn(e, "tryLock error");
        }
        return lock;
    }

    /**
     * 尝试获取指定键的锁。
     *
     * @param key 与锁关联的键
     * @return 获得的锁，如果无法获得锁，则返回null
     */
    public static Lock tryLock(String key) {
        Lock lock = null;
        try {
            lock = Future.await(VertxLoadConfig.sharedData.getLock(key));
        } catch (Exception e) {
            StaticLog.warn(e, "getLock error");
        }
        return lock;
    }

}
