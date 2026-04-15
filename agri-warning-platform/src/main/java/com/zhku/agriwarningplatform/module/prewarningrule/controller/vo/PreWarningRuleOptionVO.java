package com.zhku.agriwarningplatform.module.prewarningrule.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 15:04
 */

import lombok.Data;

@Data
public class PreWarningRuleOptionVO {

    /**
     * 显示名称
     */
    private String label;

    /**
     * 对应值
     */
    private Long value;
}
