package com.zhku.agriwarningplatform.module.warning.service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预警规则命中依据 DTO
 */
@Data
public class WarningMatchDetailDTO {

    private Long warningId;

    private String title;

    private Long ruleId;

    private String ruleName;

    private List<MatchDetailDTO> matchDetails;

    @Data
    public static class MatchDetailDTO {

        private String metric;

        private BigDecimal actualValue;

        private String unit;

        private BigDecimal minValue;

        private BigDecimal maxValue;

        private String operator;

        private Boolean matched;
    }
}