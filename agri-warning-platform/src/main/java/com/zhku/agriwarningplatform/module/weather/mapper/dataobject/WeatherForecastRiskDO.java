package com.zhku.agriwarningplatform.module.weather.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 11:02
 */
import lombok.Data;

/**
 * 天气预报每日风险概览 DO
 */
@Data
public class WeatherForecastRiskDO {

    /**
     * 日期，格式 yyyy-MM-dd
     */
    private String date;

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
}