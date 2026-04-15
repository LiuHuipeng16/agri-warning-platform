package com.zhku.agriwarningplatform.module.stats.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:42
 */

import lombok.Data;

/**
 * 作物病虫害数量统计DTO
 */
@Data
public class CropPestCountStatsDTO {

    /**
     * 作物名称
     */
    private String cropName;

    /**
     * 数量
     */
    private Long count;
}
