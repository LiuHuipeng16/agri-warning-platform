package com.zhku.agriwarningplatform.module.ai.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-07
 * Time: 20:39
 */
import lombok.Data;

import java.time.LocalDate;

@Data
public class AIImageEnvironmentRiskContextDO {

    private Long warningId;

    private String warningTitle;

    private String cropName;

    private String pestName;

    private String pestType;

    private String riskLevel;

    private Integer riskScore;

    private String warningType;

    private LocalDate warningDate;

    private String ruleName;

    private String suggestion;

    private String temperatureRange;

    private String humidityRange;

    private String environmentDescription;
}
