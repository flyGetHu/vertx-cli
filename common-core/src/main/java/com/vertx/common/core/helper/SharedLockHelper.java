package com.vertx.common.core.helper;

import com.vertx.common.core.enums.ISharedLockSharedLockEnum;
import com.vertx.common.core.utils.StrUtil;
import io.vertx.core.shareddata.Lock;

import static com.vertx.common.core.config.VertxLoadConfig.sharedData;
import static io.vertx.core.Future.await;

public class SharedLockHelper {

    /**
     * 获取锁
     *
     * @param sharedLockEnum 锁枚举
     * @param args           锁参数
     * @return 锁
     * @throws Exception 异常
     */
    public static Lock getLock(ISharedLockSharedLockEnum sharedLockEnum, String[] args) throws Exception {
        return await(sharedData.getLock(StrUtil.format(sharedLockEnum.getKey(), args)));
    }

    /**
     * 获取锁
     *
     * @param sharedLockEnum 锁枚举
     * @param block          业务代码块
     * @param args           锁参数
     * @throws Exception 异常
     */
    public static void withLock(ISharedLockSharedLockEnum sharedLockEnum, String[] args, Runnable block) throws Exception {
        Lock lock = getLock(sharedLockEnum, args);
        try {
            block.run();
        } finally {
            lock.release();
        }
    }

    /***
     * 获取锁
     * @param sharedLockEnum 锁枚举
     * @param timeout 超时时间 单位毫秒
     * @param args 锁参数
     * @return 锁
     * @throws Exception 异常
     */
    public static Lock getLockWithTimeout(ISharedLockSharedLockEnum sharedLockEnum, long timeout, String[] args) throws Exception {
        return await(sharedData.getLockWithTimeout(StrUtil.format(sharedLockEnum.getKey(), args), timeout));
    }

    public static void withLockWithTimeout(ISharedLockSharedLockEnum sharedLockEnum, long timeout, String[] args, Runnable block) throws Exception {
        Lock lock = getLockWithTimeout(sharedLockEnum, timeout, args);
        try {
            block.run();
        } finally {
            lock.release();
        }
    }

    public static Lock getLocalLock(ISharedLockSharedLockEnum sharedLockEnum, String[] args) throws Exception {
        return await(sharedData.getLocalLock(StrUtil.format(sharedLockEnum.getKey(), args)));
    }

    public static void withLocalLock(ISharedLockSharedLockEnum sharedLockEnum, String[] args, Runnable block) throws Exception {
        Lock lock = getLocalLock(sharedLockEnum, args);
        try {
            block.run();
        } finally {
            lock.release();
        }
    }

    public static Lock getLocalLockWithTimeout(ISharedLockSharedLockEnum sharedLockEnum, long timeout, String[] args) throws Exception {
        return await(sharedData.getLocalLockWithTimeout(StrUtil.format(sharedLockEnum.getKey(), args), timeout));
    }

    public static void withLocalLockWithTimeout(ISharedLockSharedLockEnum sharedLockEnum, long timeout, String[] args, Runnable block) throws Exception {
        Lock lock = getLocalLockWithTimeout(sharedLockEnum, timeout, args);
        try {
            block.run();
        } finally {
            lock.release();
        }
    }
}