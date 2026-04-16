package com.zhku.agriwarningplatform.module.stats.mapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 16:55
 */
import com.zhku.agriwarningplatform.module.stats.mapper.dataobject.*;
import org.apache.ibatis.annotations.Mapper;
import org.reactivestreams.Publisher;

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
}
