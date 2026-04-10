package com.zhku.agriwarningplatform.module.pest.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:11
 */

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class PestDO {

    /**
     * 病虫害唯一标识
     */
    private Long id;

    /**
     * 病虫害名称
     */
    private String name;

    /**
     * 病虫害类型：病害 / 虫害
     */
    private String type;

    /**
     * 病虫害详细描述
     */
    private String description;

    /**
     * 病虫害症状
     */
    private String symptoms;

    /**
     * 病虫害成因
     */
    private String cause;

    /**
     * 防治措施
     */
    private String prevention;

    /**
     * 风险等级：低 / 中 / 高
     */
    private String riskLevel;

    /**
     * 高发季节：春 / 夏 / 秋 / 冬 / 全年
     */
    private String season;

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
