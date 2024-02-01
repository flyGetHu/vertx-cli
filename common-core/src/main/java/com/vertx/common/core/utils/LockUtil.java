package com.vertx.common.core.utils;

import cn.hutool.log.StaticLog;
import com.vertx.common.core.config.VertxLoadConfig;
import io.vertx.core.Future;
import io.vertx.core.shareddata.Lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A utility class that provides local locking functionality.
 * <p>
 * This class is thread-safe.
 *
 * @author huan
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

    /**
     * 在锁内执行提供的 Runnable。使用提供的密钥获取锁。
     *
     * @param key      与锁关联的键。
     * @param runnable 在锁内执行的 Runnable。
     */
    public static void withLock(String key, Runnable runnable) {
        final Lock lock = tryLock(key);
        if (lock != null) {
            try {
                runnable.run();
            } finally {
                lock.release();
            }
        } else {
            StaticLog.error("withLock error", key);
            throw new RuntimeException("withLock error");
        }
    }

    /**
     * 在锁内执行提供的供应商。使用提供的密钥获取锁。
     *
     * @param key      与锁关联的键。
     * @param supplier 在锁内执行的供应商。
     * @param <T>      supplier返回的结果类型。
     * @return supplier返回的结果。
     */
    public static <T> T withLock(String key, Supplier<T> supplier) {
        final Lock lock = tryLock(key);
        if (lock != null) {
            try {
                return supplier.get();
            } finally {
                lock.release();
            }
        } else {
            StaticLog.error("withLock error", key);
            throw new RuntimeException("withLock error");
        }
    }

    /**
     * 使用指定的键和超时时间获取锁，并在锁定的情况下执行提供的 Runnable。
     * 如果在指定的超时时间内无法获取锁，将抛出 RuntimeException。
     *
     * @param key      与锁关联的键。
     * @param timeout  等待锁的最长时间（秒）。
     * @param runnable 需要在锁定情况下执行的 Runnable。
     * @throws RuntimeException 如果在指定的超时时间内无法获取锁。
     */
    public static void withLock(String key, long timeout, Runnable runnable) {
        final Lock lock = tryLock(key, timeout);
        if (lock != null) {
            try {
                runnable.run();
            } finally {
                lock.release();
            }
        } else {
            StaticLog.error("withLock error", key);
            throw new RuntimeException("withLock error");
        }
    }


    /**
     * 在锁内执行提供的供应商。使用提供的密钥和超时来获取锁定。
     * 如果在指定的超时时间内无法获得锁，则抛出异常。
     * 供应商预计返回 T 类型的结果。
     *
     * @param key      与锁关联的密钥。
     * @param timeout  等待锁的最长时间（以秒为单位）。
     * @param supplier 在锁内执行的供应商。
     * @return supplier返回的结果。
     * 如果在指定的超时时间内无法获得锁，则抛出 RuntimeException。
     */
    public static <T> T withLock(String key, long timeout, Supplier<T> supplier) {
        final Lock lock = tryLock(key, timeout);
        if (lock != null) {
            try {
                return supplier.get();
            } finally {
                lock.release();
            }
        } else {
            StaticLog.error("withLock error", key);
            throw new RuntimeException("withLock error");
        }
    }
}
