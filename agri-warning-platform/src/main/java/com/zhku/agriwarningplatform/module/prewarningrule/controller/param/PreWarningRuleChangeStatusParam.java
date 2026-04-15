package com.zhku.agriwarningplatform.module.prewarningrule.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 15:02
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PreWarningRuleChangeStatusParam {

    /**
     * 规则ID
     */
    @NotNull(message = "规则ID不能为空")
    private Long ruleId;

    /**
     * 目标状态：ENABLED / DISABLED
     */
    @NotBlank(message = "规则状态不能为空")
    @Size(max = 20, message = "规则状态长度不能超过20个字符")
    private String ruleStatus;
}
