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
 * 后台仪表盘统计卡片VO
 */
@Data
public class StatsDashboardVO {

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