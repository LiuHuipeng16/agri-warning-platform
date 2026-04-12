package com.zhku.agriwarningplatform.module.pestenvironment.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 22:13
 */

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PestEnvironmentDO {

    /**
     * 关联病虫害ID
     */
    private Long pestId;

    /**
     * 适宜温度范围
     */
    private String temperatureRange;

    /**
     * 适宜湿度范围
     */
    private String humidityRange;

    /**
     * 环境条件描述
     */
    private String environmentDescription;

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
