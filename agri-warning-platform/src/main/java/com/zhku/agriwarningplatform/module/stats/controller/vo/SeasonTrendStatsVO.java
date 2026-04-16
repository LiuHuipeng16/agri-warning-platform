package com.zhku.agriwarningplatform.module.stats.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:45
 */

import lombok.Data;

/**
 * 季节高发趋势统计VO
 */
@Data
public class SeasonTrendStatsVO {

    /**
     * 季节
     */
    private String season;

    /**
     * 数量
     */
    private Long count;
}
