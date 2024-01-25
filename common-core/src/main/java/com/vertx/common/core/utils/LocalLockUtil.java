package com.vertx.common.core.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import cn.hutool.log.StaticLog;

/**
 * A utility class that provides local locking functionality.
 * <p>
 * This class is thread-safe.
 */
public class LocalLockUtil {
  /**
   * A map that stores locks for different keys.
   */
  private static final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

  /**
   * Attempts to acquire the lock for the specified key within the given timeout
   * period.
   *
   * @param key     the key to acquire the lock for
   * @param timeout the maximum time to wait for the lock
   * @param unit    the time unit of the timeout parameter
   * @return true if the lock was acquired, false otherwise
   */
  public static boolean tryLock(String key, long timeout, TimeUnit unit) {
    ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
    try {
      // 如果在指定的超时时间内未能获取到锁，就返回 false
      if (!lock.tryLock(timeout, unit)) {
        StaticLog.warn("tryLock timeout, key: {}", key);
        return false;
      }
      return true;
    } catch (InterruptedException e) {
      StaticLog.error(e, "tryLock error");
      // 重置中断状态
      Thread.currentThread().interrupt();
      return false;
    }
  }

  /**
   * Unlocks the lock associated with the given key.
   * If the lock has no queued threads, it is removed from the lock map.
   *
   * @param key the key associated with the lock
   */
  public static void unlock(String key) {
    ReentrantLock lock = lockMap.get(key);
    // 检查当前线程是否持有锁
    if (lock != null && lock.isHeldByCurrentThread()) {
      lock.unlock();
      if (!lock.hasQueuedThreads()) {
        lockMap.remove(key, lock);
      }
    } else {
      StaticLog.warn("unlock error, lock is null or not held by current thread");
    }
  }
}
