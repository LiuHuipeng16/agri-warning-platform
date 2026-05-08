package com.zhku.agriwarningplatform.module.weather.mapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 11:47
 */
import com.zhku.agriwarningplatform.module.weather.mapper.dataobject.WeatherForecastRiskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 天气 Mapper
 */
@Mapper
public interface WeatherMapper {

    /**
     * 获取天气预报每日风险概览
     */
    List<WeatherForecastRiskDO> listForecastRiskStats(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate);
}
