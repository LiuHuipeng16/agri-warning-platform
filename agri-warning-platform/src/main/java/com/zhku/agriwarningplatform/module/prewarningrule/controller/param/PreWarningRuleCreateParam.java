package com.zhku.agriwarningplatform.module.prewarningrule.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 14:59
 */

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PreWarningRuleCreateParam {

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    private String ruleName;

    /**
     * 作物ID
     */
    @NotNull(message = "作物ID不能为空")
    private Long cropId;

    /**
     * 病虫害ID
     */
    @NotNull(message = "病虫害ID不能为空")
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
    @DecimalMin(value = "0", inclusive = true, message = "最低湿度不能小于0")
    private BigDecimal minHumidity;

    /**
     * 最高湿度
     */
    @DecimalMin(value = "0", inclusive = true, message = "最高湿度不能小于0")
    private BigDecimal maxHumidity;

    /**
     * 最低降雨量
     */
    @DecimalMin(value = "0", inclusive = true, message = "最低降雨量不能小于0")
    private BigDecimal minPrecipitation;

    /**
     * 最高降雨量
     */
    @DecimalMin(value = "0", inclusive = true, message = "最高降雨量不能小于0")
    private BigDecimal maxPrecipitation;

    /**
     * 最低风速
     */
    @DecimalMin(value = "0", inclusive = true, message = "最低风速不能小于0")
    private BigDecimal minWindSpeed;

    /**
     * 最高风速
     */
    @DecimalMin(value = "0", inclusive = true, message = "最高风速不能小于0")
    private BigDecimal maxWindSpeed;

    /**
     * 风险等级：低 / 中 / 高
     */
    @NotBlank(message = "风险等级不能为空")
    @Size(max = 20, message = "风险等级长度不能超过20个字符")
    private String riskLevel;

    /**
     * 防治建议
     */
    private String suggestion;

    /**
     * 规则状态：ENABLED / DISABLED
     */
    @NotBlank(message = "规则状态不能为空")
    @Size(max = 20, message = "规则状态长度不能超过20个字符")
    private String ruleStatus;
}
