package com.zhku.agriwarningplatform.module.weather.service.dto;

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
     * 当日综合风险等级
     */
    private String riskLevel;

    /**
     * 当日综合风险指数
     */
    private Integer riskScore;

    /**
     * 当日高风险预警数量
     */
    private Integer highRiskCount;

    /**
     * 当日预警总数量
     */
    private Integer warningCount;

    /**
     * 更新时间
     */
    private String updateTime;
}