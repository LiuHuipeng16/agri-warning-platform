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
 * 病害/虫害比例统计DTO
 */
@Data
public class PestTypeDistributionStatsDTO {

    /**
     * 类型名称
     */
    private String name;

    /**
     * 数量
     */
    private Long value;
}
