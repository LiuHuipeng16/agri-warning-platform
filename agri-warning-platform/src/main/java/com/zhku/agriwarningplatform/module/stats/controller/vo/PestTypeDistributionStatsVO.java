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
 * 病害/虫害比例统计VO
 */
@Data
public class PestTypeDistributionStatsVO {

    /**
     * 类型名称：病害 / 虫害
     */
    private String name;

    /**
     * 数量
     */
    private Long value;
}