package com.zhku.agriwarningplatform.module.weather.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 20:27
 */
import lombok.Data;

import java.math.BigDecimal;

/**
 * 湛江当天天气 DTO
 */
@Data
public class WeatherTodayDTO {

    /**
     * 城市名称
     */
    private String city;

    /**
     * 日期，格式：yyyy-MM-dd
     */
    private String date;

    /**
     * 当前温度
     */
    private BigDecimal temperature;

    /**
     * 当天最低温度
     */
    private BigDecimal tempMin;

    /**
     * 当天最高温度
     */
    private BigDecimal tempMax;

    /**
     * 当前相对湿度
     */
    private BigDecimal humidity;

    /**
     * 当天累计降雨量
     */
    private BigDecimal precipitation;

    /**
     * 当前风速
     */
    private BigDecimal windSpeed;

    /**
     * 天气描述
     */
    private String weatherDesc;

    /**
     * 数据更新时间
     */
    private String updateTime;
}