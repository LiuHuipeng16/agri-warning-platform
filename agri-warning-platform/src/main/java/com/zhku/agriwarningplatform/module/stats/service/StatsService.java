package com.zhku.agriwarningplatform.module.stats.service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 11:20
 */

import com.zhku.agriwarningplatform.module.stats.service.dto.CropPestCountStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.HighRiskPestStatsDTO;
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
     *
     * @return 仪表盘统计数据
     */
    StatsDashboardDTO getDashboardStats();

    /**
     * 获取作物病虫害数量统计
     *
     * @return 作物病虫害数量统计列表
     */
    List<CropPestCountStatsDTO> listCropPestCountStats();

    /**
     * 获取病害 / 虫害比例统计
     *
     * @return 病害 / 虫害比例统计列表
     */
    List<PestTypeDistributionStatsDTO> listPestTypeDistributionStats();

    /**
     * 获取高风险病虫害分布统计
     *
     * @return 高风险病虫害分布统计列表
     */
    List<HighRiskPestStatsDTO> listHighRiskPestStats();

    /**
     * 获取季节高发趋势统计
     *
     * @return 季节高发趋势统计列表
     */
    List<SeasonTrendStatsDTO> listSeasonTrendStats();
}