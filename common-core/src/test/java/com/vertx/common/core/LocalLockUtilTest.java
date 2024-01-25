package com.vertx.common.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vertx.common.core.utils.LocalLockUtil;

import java.util.concurrent.TimeUnit;

class LocalLockUtilTest {

  @Test
  void testTryLock() {
    String key = "testKey";
    long timeout = 5;
    TimeUnit unit = TimeUnit.SECONDS;

    boolean lockAcquired = LocalLockUtil.tryLock(key, timeout, unit);
    Assertions.assertTrue(lockAcquired, "Lock should be acquired successfully");

    boolean lockAcquiredAgain = LocalLockUtil.tryLock(key, timeout, unit);
    Assertions.assertFalse(lockAcquiredAgain, "Lock should not be acquired again within the timeout period");

    LocalLockUtil.unlock(key);
  }

  @Test
  void testUnlock() {
    String key = "testKey";

    // Acquire the lock
    LocalLockUtil.tryLock(key, 5, TimeUnit.SECONDS);

    // Unlock the lock
    LocalLockUtil.unlock(key);

    // Try to unlock again
    Assertions.assertDoesNotThrow(() -> LocalLockUtil.unlock(key), "Unlock should not throw an exception");
  }

  @Test
  void testUnlockWithDifferentKey() {
    String key1 = "testKey1";
    String key2 = "testKey2";

    // Acquire the lock
    LocalLockUtil.tryLock(key1, 5, TimeUnit.SECONDS);

    // Unlock the lock with a different key
    Assertions.assertDoesNotThrow(() -> LocalLockUtil.unlock(key2), "Unlock should not throw an exception");

    // Unlock the lock
    LocalLockUtil.unlock(key1);
  }
}