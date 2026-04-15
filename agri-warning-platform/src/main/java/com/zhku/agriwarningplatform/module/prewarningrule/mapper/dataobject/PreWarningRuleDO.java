package com.zhku.agriwarningplatform.module.prewarningrule.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 14:53
 */

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PreWarningRuleDO {

    /**
     * 规则ID
     */
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 作物ID
     */
    private Long cropId;

    /**
     * 病虫害ID
     */
    private Long pestId;

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
     * 风险等级：低 / 中 / 高
     */
    private String riskLevel;

    /**
     * 防治建议
     */
    private String suggestion;

    /**
     * 判断当前suggestion是否清空
     */
    private Boolean updateSuggestion;

    /**
     * 规则状态：ENABLED / DISABLED
     */
    private String ruleStatus;

    /**
     * 删除标记：0未删除，1已删除
     */
    private Integer deleteFlag;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    private LocalDateTime gmtModified;
}