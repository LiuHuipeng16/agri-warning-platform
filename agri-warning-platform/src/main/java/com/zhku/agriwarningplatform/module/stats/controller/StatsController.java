package com.zhku.agriwarningplatform.module.stats.controller;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:33
 */
import com.zhku.agriwarningplatform.common.errorcode.StatsErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.module.stats.controller.vo.CropPestCountStatsVO;
import com.zhku.agriwarningplatform.module.stats.controller.vo.HighRiskPestStatsVO;
import com.zhku.agriwarningplatform.module.stats.controller.vo.HighRiskPestTopStatsVO;
import com.zhku.agriwarningplatform.module.stats.controller.vo.PestTypeDistributionStatsVO;
import com.zhku.agriwarningplatform.module.stats.controller.vo.SeasonTrendStatsVO;
import com.zhku.agriwarningplatform.module.stats.controller.vo.StatsDashboardVO;
import com.zhku.agriwarningplatform.module.stats.service.StatsService;
import com.zhku.agriwarningplatform.module.stats.service.dto.CropPestCountStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.HighRiskPestStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.HighRiskPestTopStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.PestTypeDistributionStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.SeasonTrendStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.StatsDashboardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据统计与可视化 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private static final int DEFAULT_HIGH_RISK_PEST_TOP_LIMIT = 5;

    private static final int MIN_HIGH_RISK_PEST_TOP_LIMIT = 1;

    private static final int MAX_HIGH_RISK_PEST_TOP_LIMIT = 10;

    private final StatsService statsService;

    /**
     * 获取后台仪表盘统计数据
     *
     * 接口路径：GET /api/stats/dashboard
     * 访问角色：仅 ADMIN
     */
    @GetMapping("/dashboard")
    // @RequireRole("ADMIN")
    public CommonResult<StatsDashboardVO> getDashboardStats() {
        log.info("进入接口:StatsController#getDashboardStats");
        StatsDashboardDTO statsDashboardDTO = statsService.getDashboardStats();
        return CommonResult.success(convertToStatsDashboardVO(statsDashboardDTO));
    }

    /**
     * 获取作物病虫害数量统计
     *
     * 接口路径：GET /api/stats/cropPestCount
     * 访问角色：USER / ADMIN
     */
    @GetMapping("/cropPestCount")
    public CommonResult<List<CropPestCountStatsVO>> listCropPestCountStats() {
        log.info("进入接口:StatsController#listCropPestCountStats");
        List<CropPestCountStatsDTO> statsDTOList = statsService.listCropPestCountStats();
        List<CropPestCountStatsVO> statsVOList = statsDTOList.stream()
                .map(this::convertToCropPestCountStatsVO)
                .collect(Collectors.toList());
        return CommonResult.success(statsVOList);
    }

    /**
     * 获取病害 / 虫害比例统计
     *
     * 接口路径：GET /api/stats/pestTypeDistribution
     * 访问角色：USER / ADMIN
     */
    @GetMapping("/pestTypeDistribution")
    public CommonResult<List<PestTypeDistributionStatsVO>> listPestTypeDistributionStats() {
        log.info("进入接口:StatsController#listPestTypeDistributionStats");
        List<PestTypeDistributionStatsDTO> statsDTOList = statsService.listPestTypeDistributionStats();
        List<PestTypeDistributionStatsVO> statsVOList = statsDTOList.stream()
                .map(this::convertToPestTypeDistributionStatsVO)
                .collect(Collectors.toList());
        return CommonResult.success(statsVOList);
    }

    /**
     * 获取高风险病虫害分布统计
     *
     * 接口路径：GET /api/stats/highRiskPests
     * 访问角色：USER / ADMIN
     */
    @GetMapping("/highRiskPests")
    public CommonResult<List<HighRiskPestStatsVO>> listHighRiskPestStats() {
        log.info("进入接口:StatsController#listHighRiskPestStats");
        List<HighRiskPestStatsDTO> statsDTOList = statsService.listHighRiskPestStats();
        List<HighRiskPestStatsVO> statsVOList = statsDTOList.stream()
                .map(this::convertToHighRiskPestStatsVO)
                .collect(Collectors.toList());
        return CommonResult.success(statsVOList);
    }

    /**
     * 获取季节高发趋势统计
     *
     * 接口路径：GET /api/stats/seasonTrend
     * 访问角色：USER / ADMIN
     */
    @GetMapping("/seasonTrend")
    public CommonResult<List<SeasonTrendStatsVO>> listSeasonTrendStats() {
        log.info("进入接口:StatsController#listSeasonTrendStats");
        List<SeasonTrendStatsDTO> statsDTOList = statsService.listSeasonTrendStats();
        List<SeasonTrendStatsVO> statsVOList = statsDTOList.stream()
                .map(this::convertToSeasonTrendStatsVO)
                .collect(Collectors.toList());
        return CommonResult.success(statsVOList);
    }

    /**
     * 获取高风险病虫害排行
     *
     * 接口路径：GET /api/stats/highRiskPestTop
     * 访问角色：USER / ADMIN
     *
     * @param limit 返回前几名，默认5，范围1~10
     */
    @GetMapping("/highRiskPestTop")
    public CommonResult<List<HighRiskPestTopStatsVO>> listHighRiskPestTopStats(
            @RequestParam(value = "limit", required = false) Integer limit) {

        log.info("进入接口:StatsController#listHighRiskPestTopStats, limit={}", limit);

        Integer validLimit = limit;
        if (Objects.isNull(validLimit)) {
            validLimit = DEFAULT_HIGH_RISK_PEST_TOP_LIMIT;
        }

        if (validLimit < MIN_HIGH_RISK_PEST_TOP_LIMIT || validLimit > MAX_HIGH_RISK_PEST_TOP_LIMIT) {
            throw new ControllerException(StatsErrorCode.LIMIT_INVALID);
        }

        List<HighRiskPestTopStatsDTO> statsDTOList = statsService.listHighRiskPestTopStats(validLimit);
        List<HighRiskPestTopStatsVO> statsVOList = statsDTOList.stream()
                .map(this::convertToHighRiskPestTopStatsVO)
                .collect(Collectors.toList());

        return CommonResult.success(statsVOList);
    }

    private StatsDashboardVO convertToStatsDashboardVO(StatsDashboardDTO statsDashboardDTO) {
        StatsDashboardVO statsDashboardVO = new StatsDashboardVO();
        statsDashboardVO.setCropCount(statsDashboardDTO.getCropCount());
        statsDashboardVO.setPestCount(statsDashboardDTO.getPestCount());
        statsDashboardVO.setWarningCount(statsDashboardDTO.getWarningCount());
        statsDashboardVO.setAiQaCount(statsDashboardDTO.getAiQaCount());
        statsDashboardVO.setHighRiskCount(statsDashboardDTO.getHighRiskCount());
        statsDashboardVO.setAiImageConsultCount(statsDashboardDTO.getAiImageConsultCount());
        statsDashboardVO.setFeedbackAccuracyRate(statsDashboardDTO.getFeedbackAccuracyRate());
        return statsDashboardVO;
    }

    private CropPestCountStatsVO convertToCropPestCountStatsVO(CropPestCountStatsDTO statsDTO) {
        CropPestCountStatsVO statsVO = new CropPestCountStatsVO();
        statsVO.setCropName(statsDTO.getCropName());
        statsVO.setCount(statsDTO.getCount());
        return statsVO;
    }

    private PestTypeDistributionStatsVO convertToPestTypeDistributionStatsVO(PestTypeDistributionStatsDTO statsDTO) {
        PestTypeDistributionStatsVO statsVO = new PestTypeDistributionStatsVO();
        statsVO.setName(statsDTO.getName());
        statsVO.setValue(statsDTO.getValue());
        return statsVO;
    }

    private HighRiskPestStatsVO convertToHighRiskPestStatsVO(HighRiskPestStatsDTO statsDTO) {
        HighRiskPestStatsVO statsVO = new HighRiskPestStatsVO();
        statsVO.setPestName(statsDTO.getPestName());
        statsVO.setRiskLevel(statsDTO.getRiskLevel());
        return statsVO;
    }

    private SeasonTrendStatsVO convertToSeasonTrendStatsVO(SeasonTrendStatsDTO statsDTO) {
        SeasonTrendStatsVO statsVO = new SeasonTrendStatsVO();
        statsVO.setSeason(statsDTO.getSeason());
        statsVO.setCount(statsDTO.getCount());
        return statsVO;
    }

    private HighRiskPestTopStatsVO convertToHighRiskPestTopStatsVO(HighRiskPestTopStatsDTO statsDTO) {
        HighRiskPestTopStatsVO statsVO = new HighRiskPestTopStatsVO();
        statsVO.setRank(statsDTO.getRank());
        statsVO.setPestId(statsDTO.getPestId());
        statsVO.setPestName(statsDTO.getPestName());
        statsVO.setWarningCount(statsDTO.getWarningCount());
        statsVO.setAvgRiskScore(statsDTO.getAvgRiskScore());
        return statsVO;
    }
}