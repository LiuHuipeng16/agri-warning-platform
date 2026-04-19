package com.zhku.agriwarningplatform.module.warning.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:18
 */
import lombok.Data;

/**
 * 手动触发多天预警生成结果 VO
 */
@Data
public class WarningGenerateForecastResultVO {

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
