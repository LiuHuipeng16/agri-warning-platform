package com.zhku.agriwarningplatform.module.prewarningrule.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 15:01
 */

import com.zhku.agriwarningplatform.common.page.PageParam;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PreWarningRulePageParam extends PageParam {
    /**
     * 规则名称关键词
     */
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
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
     * 风险等级
     */
    @Size(max = 20, message = "风险等级长度不能超过20个字符")
    private String riskLevel;

    /**
     * 规则状态
     */
    @Size(max = 20, message = "规则状态长度不能超过20个字符")
    private String ruleStatus;
}