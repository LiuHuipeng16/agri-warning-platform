package com.zhku.agriwarningplatform.module.warning.controller.vo;

import lombok.Data;

import java.util.List;

/**
 * 预警风险评分 VO
 */
@Data
public class WarningRiskScoreVO {

    /**
     * 预警ID
     */
    private Long warningId;

    /**
     * 预警标题
     */
    private String title;

    /**
     * 风险评分
     */
    private Integer riskScore;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 风险评分明细
     */
    private List<ScoreDetail> scoreDetails;

    @Data
    public static class ScoreDetail {

        /**
         * 因子名称
         */
        private String factor;

        /**
         * 当前得分
         */
        private Integer score;

        /**
         * 最大得分
         */
        private Integer maxScore;
    }
}
