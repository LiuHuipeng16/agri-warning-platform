package com.zhku.agriwarningplatform.module.ai.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-07
 * Time: 20:30
 */
import lombok.Data;

import java.time.LocalDate;

@Data
public class AIWarningExplanationContextDO {

    private Long warningId;

    private String warningTitle;

    private Long cropId;

    private String cropName;

    private Long pestId;

    private String pestName;

    private String pestType;

    private String symptoms;

    private String cause;

    private String prevention;

    private String riskLevel;

    private Integer riskScore;

    private String riskScoreDetail;

    private String matchDetail;

    private String warningType;

    private LocalDate warningDate;

    private Long ruleId;

    private String ruleName;

    private String suggestion;
}
