package com.zhku.agriwarningplatform.module.warning.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:06
 */
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 预警生成上下文 DTO
 */
@Data
public class WarningGenerateContextDTO {

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 作物ID
     */
    private Long cropId;

    /**
     * 作物名称
     */
    private String cropName;

    /**
     * 病虫害ID
     */
    private Long pestId;

    /**
     * 病虫害名称
     */
    private String pestName;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 防治建议
     */
    private String suggestion;

    /**
     * 预警类型
     */
    private String warningType;

    /**
     * 预警日期
     */
    private LocalDate warningDate;

    /**
     * 温度
     */
    private BigDecimal temperature;

    /**
     * 湿度
     */
    private BigDecimal humidity;

    /**
     * 降雨量
     */
    private BigDecimal precipitation;

    /**
     * 风速
     */
    private BigDecimal windSpeed;
}
