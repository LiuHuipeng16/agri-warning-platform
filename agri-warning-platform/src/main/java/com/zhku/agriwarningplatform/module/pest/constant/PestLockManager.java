package com.zhku.agriwarningplatform.module.pest.constant;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:20
 */

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class PestLockManager {

    private static final Map<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<>();

    private PestLockManager() {
    }

    public static ReentrantLock getLock(String key) {
        Objects.requireNonNull(key, "lock key cannot be null");
        return LOCK_MAP.computeIfAbsent(key, k -> new ReentrantLock());
    }

    public static String buildPestIdLockKey(Long pestId) {
        return "pest:id:" + pestId;
    }

    public static String buildPestNameLockKey(String pestName) {
        return "pest:name:" + pestName;
    }

    public static String buildPestEnvironmentLockKey(Long pestId) {
        return "pest:environment:" + pestId;
    }
}
