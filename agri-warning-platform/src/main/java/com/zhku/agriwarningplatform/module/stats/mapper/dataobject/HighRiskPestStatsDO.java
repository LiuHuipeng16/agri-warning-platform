package com.zhku.agriwarningplatform.module.stats.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:41
 */

import lombok.Data;

/**
 * 高风险病虫害分布统计
 */
@Data
public class HighRiskPestStatsDO {

    /**
     * 病虫害名称
     */
    private String pestName;

    /**
     * 风险等级
     */
    private String riskLevel;
}