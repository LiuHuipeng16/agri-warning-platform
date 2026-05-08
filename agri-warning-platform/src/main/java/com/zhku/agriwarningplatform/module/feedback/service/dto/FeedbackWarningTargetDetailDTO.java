package com.zhku.agriwarningplatform.module.feedback.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:33
 */
import lombok.Data;

import java.time.LocalDate;

/**
 * 反馈目标预警摘要 DTO
 */
@Data
public class FeedbackWarningTargetDetailDTO {

    private Long warningId;

    private String title;

    private String cropName;

    private String pestName;

    private LocalDate warningDate;

    private String warningType;

    private String riskLevel;

    private Integer riskScore;
}
