package com.zhku.agriwarningplatform.common.config;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-17
 * Time: 15:52
 */
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 应用启动状态标志
 */
@Slf4j
@Component
public class ApplicationStartupFlag {

    /**
     * 系统是否已完全启动
     */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * SpringBoot 完全启动后触发
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        STARTED.set(true);
        log.info("系统启动完成，定时任务开始允许执行");
    }

    /**
     * 是否允许执行定时任务
     */
    public static boolean isStarted() {
        return STARTED.get();
    }

}