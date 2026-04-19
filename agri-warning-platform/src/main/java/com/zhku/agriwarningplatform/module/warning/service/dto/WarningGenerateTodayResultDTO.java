package com.zhku.agriwarningplatform.module.warning.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:05
 */
import lombok.Data;

import java.time.LocalDate;

/**
 * 手动触发当天预警生成结果 DTO
 */
@Data
public class WarningGenerateTodayResultDTO {

    /**
     * 生成成功数量
     */
    private Integer generatedCount;

    /**
     * 跳过数量
     */
    private Integer skippedCount;

    /**
     * 预警日期
     */
    private LocalDate warningDate;
}
