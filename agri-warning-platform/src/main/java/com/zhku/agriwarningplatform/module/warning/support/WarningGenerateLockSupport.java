package com.zhku.agriwarningplatform.module.warning.support;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 17:08
 */

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 预警生成任务互斥锁
 */
@Component
public class WarningGenerateLockSupport {

    /**
     * 公平锁，避免长期抢不到
     */
    private final ReentrantLock lock = new ReentrantLock(true);

    /**
     * 尝试获取锁
     *
     * @return true-获取成功；false-获取失败
     */
    public boolean tryLock() {
        return lock.tryLock();
    }

    /**
     * 释放锁
     */
    public void unlock() {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 当前线程是否持有锁
     *
     * @return 是否持有锁
     */
    public boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }
}
