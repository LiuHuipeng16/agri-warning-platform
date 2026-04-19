package com.zhku.agriwarningplatform.module.warning.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:06
 */
import lombok.Data;

/**
 * 手动触发多天预警生成结果 DTO
 */
@Data
public class WarningGenerateForecastResultDTO {

    /**
     * 生成成功数量
     */
    private Integer generatedCount;

    /**
     * 跳过数量
     */
    private Integer skippedCount;

    /**
     * 生成天数
     */
    private Integer days;
}