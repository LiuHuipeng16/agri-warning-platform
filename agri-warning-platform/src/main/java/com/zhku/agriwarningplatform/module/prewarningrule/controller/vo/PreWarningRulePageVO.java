package com.zhku.agriwarningplatform.module.prewarningrule.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 15:03
 */

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PreWarningRulePageVO {

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
     * 规则状态
     */
    private String ruleStatus;

    /**
     * 最低温度
     */
    private BigDecimal minTemp;

    /**
     * 最高温度
     */
    private BigDecimal maxTemp;

    /**
     * 最低湿度
     */
    private BigDecimal minHumidity;

    /**
     * 最高湿度
     */
    private BigDecimal maxHumidity;

    /**
     * 最低降雨量
     */
    private BigDecimal minPrecipitation;

    /**
     * 最高降雨量
     */
    private BigDecimal maxPrecipitation;

    /**
     * 最低风速
     */
    private BigDecimal minWindSpeed;

    /**
     * 最高风速
     */
    private BigDecimal maxWindSpeed;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
