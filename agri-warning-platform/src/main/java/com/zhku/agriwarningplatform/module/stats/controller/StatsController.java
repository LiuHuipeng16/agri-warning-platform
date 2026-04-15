package com.zhku.agriwarningplatform.module.stats.controller;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:33
 */
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.module.stats.controller.vo.CropPestCountStatsVO;
import com.zhku.agriwarningplatform.module.stats.controller.vo.HighRiskPestStatsVO;
import com.zhku.agriwarningplatform.module.stats.controller.vo.PestTypeDistributionStatsVO;
import com.zhku.agriwarningplatform.module.stats.controller.vo.SeasonTrendStatsVO;
import com.zhku.agriwarningplatform.module.stats.controller.vo.StatsDashboardVO;
import com.zhku.agriwarningplatform.module.stats.service.StatsService;
import com.zhku.agriwarningplatform.module.stats.service.dto.CropPestCountStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.HighRiskPestStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.PestTypeDistributionStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.SeasonTrendStatsDTO;
import com.zhku.agriwarningplatform.module.stats.service.dto.StatsDashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据统计与可视化 Controller
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 获取后台仪表盘统计数据
     *
     * 接口路径：GET /api/stats/dashboard
     * 访问角色：仅 ADMIN
     *
     * @return 后台仪表盘统计数据
     */
    @GetMapping("/dashboard")
    // @RequireRole("ADMIN")
    public CommonResult<StatsDashboardVO> getDashboardStats() {
        StatsDashboardDTO statsDashboardDTO = statsService.getDashboardStats();
        return CommonResult.success(convertToStatsDashboardVO(statsDashboardDTO));
    }

    /**
     * 获取作物病虫害数量统计
     *
     * 接口路径：GET /api/stats/cropPestCount
     * 访问角色：USER / ADMIN
     *
     * @return 作物病虫害数量统计列表
     */
    @GetMapping("/cropPestCount")
    public CommonResult<List<CropPestCountStatsVO>> listCropPestCountStats() {
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
     *
     * @return 病害 / 虫害比例统计列表
     */
    @GetMapping("/pestTypeDistribution")
    public CommonResult<List<PestTypeDistributionStatsVO>> listPestTypeDistributionStats() {
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
     *
     * @return 高风险病虫害分布统计列表
     */
    @GetMapping("/highRiskPests")
    public CommonResult<List<HighRiskPestStatsVO>> listHighRiskPestStats() {
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
     *
     * @return 季节高发趋势统计列表
     */
    @GetMapping("/seasonTrend")
    public CommonResult<List<SeasonTrendStatsVO>> listSeasonTrendStats() {
        List<SeasonTrendStatsDTO> statsDTOList = statsService.listSeasonTrendStats();
        List<SeasonTrendStatsVO> statsVOList = statsDTOList.stream()
                .map(this::convertToSeasonTrendStatsVO)
                .collect(Collectors.toList());
        return CommonResult.success(statsVOList);
    }

    private StatsDashboardVO convertToStatsDashboardVO(StatsDashboardDTO statsDashboardDTO) {
        StatsDashboardVO statsDashboardVO = new StatsDashboardVO();
        statsDashboardVO.setCropCount(statsDashboardDTO.getCropCount());
        statsDashboardVO.setPestCount(statsDashboardDTO.getPestCount());
        statsDashboardVO.setWarningCount(statsDashboardDTO.getWarningCount());
        statsDashboardVO.setAiQaCount(statsDashboardDTO.getAiQaCount());
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
}