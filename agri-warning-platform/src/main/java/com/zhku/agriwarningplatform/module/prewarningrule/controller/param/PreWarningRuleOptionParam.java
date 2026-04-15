package com.zhku.agriwarningplatform.module.prewarningrule.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 15:02
 */

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PreWarningRuleOptionParam {

    /**
     * 规则状态：ENABLED / DISABLED
     */
    @Size(max = 20, message = "规则状态长度不能超过20个字符")
    private String ruleStatus;
}
