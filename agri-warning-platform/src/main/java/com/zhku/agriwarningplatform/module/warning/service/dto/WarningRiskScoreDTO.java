package com.zhku.agriwarningplatform.module.warning.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-07
 * Time: 11:20
 */
import lombok.Data;

import java.util.List;

/**
 * 预警风险评分 DTO
 */
@Data
public class WarningRiskScoreDTO {

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
    private List<ScoreDetailDTO> scoreDetails;

    @Data
    public static class ScoreDetailDTO {

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