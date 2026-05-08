package com.zhku.agriwarningplatform.module.feedback.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:36
 */
import lombok.Data;

import java.time.LocalDate;

/**
 * 反馈目标预警摘要 VO
 */
@Data
public class FeedbackWarningTargetDetailVO {

    /**
     * 预警ID
     */
    private Long warningId;

    /**
     * 预警标题
     */
    private String title;

    /**
     * 作物名称
     */
    private String cropName;

    /**
     * 病虫害名称
     */
    private String pestName;

    /**
     * 预警日期
     */
    private LocalDate warningDate;

    /**
     * 预警类型
     */
    private String warningType;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 风险指数
     */
    private Integer riskScore;
}
