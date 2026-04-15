package com.zhku.agriwarningplatform.module.stats.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:40
 */

import lombok.Data;

/**
 * 作物病虫害数量统计
 */
@Data
public class CropPestCountStatsDO {

    /**
     * 作物名称
     */
    private String cropName;

    /**
     * 数量
     */
    private Long count;
}
