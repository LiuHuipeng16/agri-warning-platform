package com.zhku.agriwarningplatform.module.prewarningrule.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 14:58
 */

import lombok.Data;

@Data
public class PreWarningRuleOptionDTO {

    /**
     * 规则ID
     */
    private Long value;

    /**
     * 规则名称
     */
    private String label;
}
