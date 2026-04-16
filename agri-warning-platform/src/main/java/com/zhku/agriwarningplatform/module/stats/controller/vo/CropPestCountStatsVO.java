package com.zhku.agriwarningplatform.module.stats.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:44
 */

import lombok.Data;

/**
 * 作物病虫害数量统计VO
 */
@Data
public class CropPestCountStatsVO {

    /**
     * 作物名称
     */
    private String cropName;

    /**
     * 数量
     */
    private Long count;
}
