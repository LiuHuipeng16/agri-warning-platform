package com.zhku.agriwarningplatform.module.stats.service.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台仪表盘统计卡片 DTO
 */
@Data
public class StatsDashboardDTO {

    /**
     * 作物数量
     */
    private Long cropCount;

    /**
     * 病虫害数量
     */
    private Long pestCount;

    /**
     * 预警总数
     */
    private Long warningCount;

    /**
     * AI问答总次数
     */
    private Long aiQaCount;

    /**
     * 高风险预警数量
     */
    private Long highRiskCount;

    /**
     * AI图文问诊次数
     */
    private Long aiImageConsultCount;

    /**
     * 用户反馈准确率，单位：%
     */
    private BigDecimal feedbackAccuracyRate;
}