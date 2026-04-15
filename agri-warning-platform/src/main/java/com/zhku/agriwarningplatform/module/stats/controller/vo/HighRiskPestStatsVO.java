package com.zhku.agriwarningplatform.module.stats.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:45
 */

import lombok.Data;

/**
 * 高风险病虫害分布统计VO
 */
@Data
public class HighRiskPestStatsVO {

    /**
     * 病虫害名称
     */
    private String pestName;

    /**
     * 风险等级
     */
    private String riskLevel;
}
