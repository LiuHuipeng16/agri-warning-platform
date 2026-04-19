package com.zhku.agriwarningplatform.module.warning.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:03
 */
import lombok.Data;

import java.time.LocalDate;

/**
 * 预警分页查询 DTO
 */
@Data
public class WarningPageQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 预警标题关键词
     */
    private String title;

    /**
     * 作物ID
     */
    private Long cropId;

    /**
     * 病虫害ID
     */
    private Long pestId;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 预警类型
     */
    private String warningType;

    /**
     * 预警日期开始
     */
    private LocalDate warningDateStart;

    /**
     * 预警日期结束
     */
    private LocalDate warningDateEnd;
}
