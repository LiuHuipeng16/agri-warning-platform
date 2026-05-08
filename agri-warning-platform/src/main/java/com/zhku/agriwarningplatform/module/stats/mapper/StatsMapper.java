package com.zhku.agriwarningplatform.module.stats.mapper;

import com.zhku.agriwarningplatform.module.stats.mapper.dataobject.*;
import com.zhku.agriwarningplatform.module.weather.mapper.dataobject.WeatherForecastRiskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据统计与可视化 Mapper
 */
@Mapper
public interface StatsMapper {

    /**
     * 获取后台仪表盘统计卡片数据
     *
     * @return 仪表盘统计数据
     */
    StatsDashboardDO getDashboardStats();

    /**
     * 获取作物病虫害数量统计
     *
     * @return 作物病虫害数量统计列表
     */
    List<CropPestCountStatsDO> listCropPestCountStats();

    /**
     * 获取病害 / 虫害比例统计
     *
     * @return 病害 / 虫害比例统计列表
     */
    List<PestTypeDistributionStatsDO> listPestTypeDistributionStats();

    /**
     * 获取高风险病虫害分布
     *
     * @return 高风险病虫害列表
     */
    List<HighRiskPestStatsDO> listHighRiskPestStats();

    /**
     * 获取季节高发趋势统计
     *
     * @return 季节高发趋势统计列表
     */
    List<SeasonTrendStatsDO> listSeasonTrendStats();

    /**
     * 获取高风险病虫害排行
     *
     * @param limit 返回数量
     * @return 高风险病虫害排行
     */
    List<HighRiskPestTopStatsDO> listHighRiskPestTopStats(@Param("limit") Integer limit);

    /**
     * 获取天气预报每日风险概览
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 风险概览列表
     */
    List<WeatherForecastRiskDO> listWeatherForecastRiskStats(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}