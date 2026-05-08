package com.zhku.agriwarningplatform.module.stats.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 11:13
 */
import lombok.Data;

import java.math.BigDecimal;

/**
 * 高风险病虫害排行 VO
 */
@Data
public class HighRiskPestTopStatsVO {

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 病虫害ID
     */
    private Long pestId;

    /**
     * 病虫害名称
     */
    private String pestName;

    /**
     * 高风险预警次数
     */
    private Long warningCount;

    /**
     * 平均风险指数
     */
    private BigDecimal avgRiskScore;
}