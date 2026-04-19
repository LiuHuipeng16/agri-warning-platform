package com.zhku.agriwarningplatform.module.ai.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:24
 */
import lombok.Data;

import java.time.LocalDate;

@Data
public class AIWarningSuggestionContextDO {

    /**
     * 预警ID
     */
    private Long warningId;

    /**
     * 预警标题
     */
    private String warningTitle;

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
     * 病虫害类型
     */
    private String pestType;

    /**
     * 病虫害症状
     */
    private String symptoms;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 预警类型
     */
    private String warningType;

    /**
     * 预警日期
     */
    private LocalDate warningDate;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 基础建议
     */
    private String suggestion;
}
