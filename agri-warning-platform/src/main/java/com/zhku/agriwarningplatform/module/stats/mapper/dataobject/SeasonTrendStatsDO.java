package com.zhku.agriwarningplatform.module.stats.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:41
 */

import lombok.Data;

/**
 * 季节高发趋势统计
 */
@Data
public class SeasonTrendStatsDO {

    /**
     * 季节
     */
    private String season;

    /**
     * 数量
     */
    private Long count;
}
