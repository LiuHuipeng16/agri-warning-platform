package com.zhku.agriwarningplatform.module.weather.service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 20:28
 */

import com.zhku.agriwarningplatform.module.weather.service.dto.WeatherForecastDTO;
import com.zhku.agriwarningplatform.module.weather.service.dto.WeatherTodayDTO;

import java.util.List;

/**
 * 天气 Service
 */
public interface WeatherService {

    /**
     * 获取湛江当天天气
     *
     * @return 当天天气信息
     */
    WeatherTodayDTO getTodayWeather();


    /**
     * 获取湛江多天天气预报
     *
     * @param days 未来天数
     * @return 多天天气预报
     */
    List<WeatherForecastDTO> getForecastWeather(Integer days);
}