package com.zhku.agriwarningplatform.module.stats.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:43
 */
import lombok.Data;

/**
 * 季节高发趋势统计DTO
 */
@Data
public class SeasonTrendStatsDTO {

    /**
     * 季节
     */
    private String season;

    /**
     * 数量
     */
    private Long count;
}