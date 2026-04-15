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
 * 后台仪表盘统计卡片DTO
 */
@Data
public class StatsDashboardDTO {

    /**
     * 作物数量
     */
    private Long cropCount;

    /**
     * 病虫害数量
     */
    private Long pestCount;

    /**
     * 预警数量
     */
    private Long warningCount;

    /**
     * AI问答数量
     */
    private Long aiQaCount;
}
