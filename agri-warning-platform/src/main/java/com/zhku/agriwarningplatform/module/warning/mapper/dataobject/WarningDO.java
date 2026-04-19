package com.zhku.agriwarningplatform.module.warning.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:01
 */
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预警表 DO
 */
@TableName("warning")
@Data
public class WarningDO {

    /**
     * 预警ID
     */
    private Long id;

    /**
     * 预警标题
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
     * 风险等级：低 / 中 / 高
     */
    private String riskLevel;

    /**
     * 预警类型：TODAY / FORECAST
     */
    private String warningType;

    /**
     * 预警日期
     */
    private LocalDate warningDate;

    /**
     * 命中的规则ID
     */
    private Long ruleId;

    /**
     * 删除标记：0未删除，1已删除
     */
    private Integer deleteFlag;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    private LocalDateTime gmtModified;
}