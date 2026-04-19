package com.zhku.agriwarningplatform.module.weather.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 21:40
 */
import lombok.Data;

import java.math.BigDecimal;

/**
 * 多天天气预报 DTO
 */
@Data
public class WeatherForecastDTO {

    /**
     * 城市名称
     */
    private String city;

    /**
     * 日期
     */
    private String date;

    /**
     * 最低温度
     */
    private BigDecimal tempMin;

    /**
     * 最高温度
     */
    private BigDecimal tempMax;

    /**
     * 平均湿度
     */
    private BigDecimal avgHumidity;

    /**
     * 累计降雨量
     */
    private BigDecimal precipitation;

    /**
     * 最大风速
     */
    private BigDecimal maxWindSpeed;

    /**
     * 天气描述
     */
    private String weatherDesc;

    /**
     * 更新时间
     */
    private String updateTime;
}
