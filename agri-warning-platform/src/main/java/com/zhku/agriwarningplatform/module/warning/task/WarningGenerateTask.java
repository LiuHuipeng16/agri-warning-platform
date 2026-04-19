package com.zhku.agriwarningplatform.module.warning.task;

import com.zhku.agriwarningplatform.module.warning.service.WarningService;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningGenerateForecastResultDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningGenerateTodayResultDTO;
import com.zhku.agriwarningplatform.module.warning.support.WarningGenerateLockSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预警自动生成定时任务
 */
@Slf4j
@Component
public class WarningGenerateTask {

    /**
     * 默认生成未来预警天数
     */
    private static final int DEFAULT_FORECAST_DAYS = 5;

    private final WarningService warningService;
    private final WarningGenerateLockSupport warningGenerateLockSupport;

    public WarningGenerateTask(WarningService warningService,
                               WarningGenerateLockSupport warningGenerateLockSupport) {
        this.warningService = warningService;
        this.warningGenerateLockSupport = warningGenerateLockSupport;
    }

    /**
     * 每天凌晨 00:00 执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateMidnightWarnings() {
        executeGenerateTask("凌晨00:00");
    }

    /**
     * 每天中午 12:00 执行
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void generateNoonWarnings() {
        executeGenerateTask("中午12:00");
    }

    /**
     * 每天下午 18:00 执行
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void generateEveningWarnings() {
        executeGenerateTask("下午18:00");
    }

    /**
     * 执行预警生成任务
     */
    private void executeGenerateTask(String timePoint) {
        if (!warningGenerateLockSupport.tryLock()) {
            log.warn("{}预警生成任务跳过：当前已有预警生成任务正在执行", timePoint);
            return;
        }

        try {
            log.info("开始执行{}预警生成任务", timePoint);

            WarningGenerateTodayResultDTO todayResult =
                    warningService.refreshTodayWarningsForTask();

            WarningGenerateForecastResultDTO forecastResult =
                    warningService.refreshForecastWarningsForTask(DEFAULT_FORECAST_DAYS);

            log.info("{}预警生成完成，todayResult={}, forecastResult={}",
                    timePoint, todayResult, forecastResult);

        } catch (Exception e) {
            log.error("{}预警生成任务异常", timePoint, e);
        } finally {
            warningGenerateLockSupport.unlock();
        }
    }
}