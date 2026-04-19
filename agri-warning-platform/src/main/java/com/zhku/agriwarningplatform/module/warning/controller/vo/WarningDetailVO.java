package com.zhku.agriwarningplatform.module.warning.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:08
 */
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预警详情 VO
 */
@Data
public class WarningDetailVO {

    /**
     * 预警ID
     */
    private Long warningId;

    /**
     * 预警标题
     */
    private String title;

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
     * 预警类型
     */
    private String warningType;

    /**
     * 预警日期
     */
    private LocalDate warningDate;

    /**
     * 命中规则ID
     */
    private Long ruleId;

    /**
     * 防治建议
     */
    private String suggestion;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
