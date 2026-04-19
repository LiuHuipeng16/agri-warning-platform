package com.zhku.agriwarningplatform.module.warning.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 15:06
 */
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 规则匹配天气数据 DTO
 */
@Data
public class WarningMatchWeatherDTO {

    /**
     * 对应日期
     */
    private LocalDate date;

    /**
     * 平均温度
     */
    private BigDecimal temperature;

    /**
     * 平均湿度
     */
    private BigDecimal humidity;

    /**
     * 日累计降雨量
     */
    private BigDecimal precipitation;

    /**
     * 平均风速
     */
    private BigDecimal windSpeed;
}