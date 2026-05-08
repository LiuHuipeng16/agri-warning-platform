package com.zhku.agriwarningplatform.module.stats.service;

import com.zhku.agriwarningplatform.module.stats.service.dto.CropPestCountStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.HighRiskPestStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.HighRiskPestTopStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.PestTypeDistributionStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.SeasonTrendStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.StatsDashboardDTO;

import java.util.List;

/**
 * 数据统计与可视化 Service
 */
public interface StatsService {

    /**
     * 获取后台仪表盘统计数据
     */
    StatsDashboardDTO getDashboardStats();

    /**
     * 获取作物病虫害数量统计
     */
    List<CropPestCountStatsDTO> listCropPestCountStats();

    /**
     * 获取病害 / 虫害比例统计
     */
    List<PestTypeDistributionStatsDTO> listPestTypeDistributionStats();

    /**
     * 获取高风险病虫害分布统计
     */
    List<HighRiskPestStatsDTO> listHighRiskPestStats();

    /**
     * 获取季节高发趋势统计
     */
    List<SeasonTrendStatsDTO> listSeasonTrendStats();

    /**
     * 获取高风险病虫害排行
     *
     * @param limit 返回数量
     */
    List<HighRiskPestTopStatsDTO> listHighRiskPestTopStats(Integer limit);
}