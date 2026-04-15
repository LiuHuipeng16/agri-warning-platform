package com.zhku.agriwarningplatform.module.stats.service.impl;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 17:02
 */

import com.zhku.agriwarningplatform.common.errorcode.StatsErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.module.stats.mapper.StatsMapper;
import com.zhku.agriwarningplatform.module.stats.mapper.dataobject.CropPestCountStatsDO;
import com.zhku.agriwarningplatform.module.stats.mapper.dataobject.HighRiskPestStatsDO;
import com.zhku.agriwarningplatform.module.stats.mapper.dataobject.PestTypeDistributionStatsDO;
import com.zhku.agriwarningplatform.module.stats.mapper.dataobject.SeasonTrendStatsDO;
import com.zhku.agriwarningplatform.module.stats.mapper.dataobject.StatsDashboardDO;
import com.zhku.agriwarningplatform.module.stats.service.StatsService;
import com.zhku.agriwarningplatform.module.stats.service.dto.CropPestCountStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.HighRiskPestStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.PestTypeDistributionStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.SeasonTrendStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.StatsDashboardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据统计与可视化 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;

    @Override
    public StatsDashboardDTO getDashboardStats() {
        try {
            StatsDashboardDO statsDashboardDO = statsMapper.getDashboardStats();
            if (Objects.isNull(statsDashboardDO)) {
                throw new ServiceException(StatsErrorCode.DATA_NOT_EXIST);
            }
            return convertToStatsDashboardDTO(statsDashboardDO);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取后台仪表盘统计数据异常", e);
            throw new ServiceException(StatsErrorCode.DASHBOARD_QUERY_FAILED);
        }
    }

    @Override
    public List<CropPestCountStatsDTO> listCropPestCountStats() {
        try {
            List<CropPestCountStatsDO> statsDOList = statsMapper.listCropPestCountStats();
            if (statsDOList == null || statsDOList.isEmpty()) {
                return Collections.emptyList();
            }
            return statsDOList.stream()
                    .map(this::convertToCropPestCountStatsDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取作物病虫害数量统计异常", e);
            throw new ServiceException(StatsErrorCode.CROP_PEST_COUNT_QUERY_FAILED);
        }
    }

    @Override
    public List<PestTypeDistributionStatsDTO> listPestTypeDistributionStats() {
        try {
            List<PestTypeDistributionStatsDO> statsDOList = statsMapper.listPestTypeDistributionStats();
            if (statsDOList == null || statsDOList.isEmpty()) {
                return Collections.emptyList();
            }
            return statsDOList.stream()
                    .map(this::convertToPestTypeDistributionStatsDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取病害/虫害比例统计异常", e);
            throw new ServiceException(StatsErrorCode.PEST_TYPE_DISTRIBUTION_QUERY_FAILED);
        }
    }

    @Override
    public List<HighRiskPestStatsDTO> listHighRiskPestStats() {
        try {
            List<HighRiskPestStatsDO> statsDOList = statsMapper.listHighRiskPestStats();
            if (statsDOList == null || statsDOList.isEmpty()) {
                return Collections.emptyList();
            }
            return statsDOList.stream()
                    .map(this::convertToHighRiskPestStatsDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取高风险病虫害分布统计异常", e);
            throw new ServiceException(StatsErrorCode.HIGH_RISK_PESTS_QUERY_FAILED);
        }
    }

    @Override
    public List<SeasonTrendStatsDTO> listSeasonTrendStats() {
        try {
            List<SeasonTrendStatsDO> statsDOList = statsMapper.listSeasonTrendStats();
            if (statsDOList == null || statsDOList.isEmpty()) {
                return Collections.emptyList();
            }
            return statsDOList.stream()
                    .map(this::convertToSeasonTrendStatsDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取季节高发趋势统计异常", e);
            throw new ServiceException(StatsErrorCode.SEASON_TREND_QUERY_FAILED);
        }
    }

    private StatsDashboardDTO convertToStatsDashboardDTO(StatsDashboardDO statsDashboardDO) {
        StatsDashboardDTO statsDashboardDTO = new StatsDashboardDTO();
        statsDashboardDTO.setCropCount(statsDashboardDO.getCropCount());
        statsDashboardDTO.setPestCount(statsDashboardDO.getPestCount());
        statsDashboardDTO.setWarningCount(statsDashboardDO.getWarningCount());
        statsDashboardDTO.setAiQaCount(statsDashboardDO.getAiQaCount());
        return statsDashboardDTO;
    }

    private CropPestCountStatsDTO convertToCropPestCountStatsDTO(CropPestCountStatsDO statsDO) {
        CropPestCountStatsDTO statsDTO = new CropPestCountStatsDTO();
        statsDTO.setCropName(statsDO.getCropName());
        statsDTO.setCount(statsDO.getCount());
        return statsDTO;
    }

    private PestTypeDistributionStatsDTO convertToPestTypeDistributionStatsDTO(PestTypeDistributionStatsDO statsDO) {
        PestTypeDistributionStatsDTO statsDTO = new PestTypeDistributionStatsDTO();
        statsDTO.setName(statsDO.getName());
        statsDTO.setValue(statsDO.getValue());
        return statsDTO;
    }

    private HighRiskPestStatsDTO convertToHighRiskPestStatsDTO(HighRiskPestStatsDO statsDO) {
        HighRiskPestStatsDTO statsDTO = new HighRiskPestStatsDTO();
        statsDTO.setPestName(statsDO.getPestName());
        statsDTO.setRiskLevel(statsDO.getRiskLevel());
        return statsDTO;
    }

    private SeasonTrendStatsDTO convertToSeasonTrendStatsDTO(SeasonTrendStatsDO statsDO) {
        SeasonTrendStatsDTO statsDTO = new SeasonTrendStatsDTO();
        statsDTO.setSeason(statsDO.getSeason());
        statsDTO.setCount(statsDO.getCount());
        return statsDTO;
    }
}
