package com.zhku.agriwarningplatform.module.feedback.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:30
 */
import lombok.Data;

import java.time.LocalDate;

/**
 * 反馈目标预警摘要 DO
 */
@Data
public class FeedbackWarningTargetDetailDO {

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
