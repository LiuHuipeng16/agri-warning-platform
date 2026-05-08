package com.zhku.agriwarningplatform.module.warning.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-07
 * Time: 11:21
 */
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预警规则命中依据 VO
 */
@Data
public class WarningMatchDetailVO {

    private Long warningId;

    private String title;

    private Long ruleId;

    private String ruleName;

    private List<MatchDetail> matchDetails;

    @Data
    public static class MatchDetail {

        private String metric;

        private BigDecimal actualValue;

        private String unit;

        private BigDecimal minValue;

        private BigDecimal maxValue;

        private String operator;

        private Boolean matched;
    }
}