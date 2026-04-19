package com.zhku.agriwarningplatform.module.warning.service.impl;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-16
 * Time: 14:58
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhku.agriwarningplatform.common.errorcode.ErrorCode;
import com.zhku.agriwarningplatform.common.errorcode.WarningErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.pest.mapper.PestMapper;
import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.PestDO;
import com.zhku.agriwarningplatform.module.prewarningrule.mapper.PreWarningRuleMapper;
import com.zhku.agriwarningplatform.module.prewarningrule.mapper.dataobject.PreWarningRuleDO;
import com.zhku.agriwarningplatform.module.warning.mapper.WarningMapper;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningDO;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningDetailDO;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningListItemDO;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningPageQueryDO;
import com.zhku.agriwarningplatform.module.warning.service.WarningService;
import com.zhku.agriwarningplatform.module.warning.service.dto.ForecastWarningDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.TodayWarningDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningDetailDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningGenerateForecastResultDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningGenerateTodayResultDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningPageItemDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningPageQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zhku.agriwarningplatform.module.warning.support.WarningGenerateLockSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 预警 Service 实现类
 */
@Slf4j
@Service
public class WarningServiceImpl implements WarningService {

    /**
     * 湛江经纬度
     */
    private static final double ZHANJIANG_LATITUDE = 21.2707D;

    private static final double ZHANJIANG_LONGITUDE = 110.3594D;
    private final WarningGenerateLockSupport warningGenerateLockSupport;

    /**
     * Open-Meteo 预报接口
     */
    private static final String OPEN_METEO_FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    /**
     * 获取天气时真正需要的小时级字段
     */
    private static final String OPEN_METEO_HOURLY_VARS =
            "temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m";

    /**
     * forecast 接口默认天数
     */
    private static final int DEFAULT_FORECAST_DAYS = 4;

    private final WarningMapper warningMapper;
    private final PreWarningRuleMapper preWarningRuleMapper;
    private final CropMapper cropMapper;
    private final PestMapper pestMapper;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    public WarningServiceImpl(WarningMapper warningMapper,
                              PreWarningRuleMapper preWarningRuleMapper,
                              CropMapper cropMapper,
                              PestMapper pestMapper,
                              WarningGenerateLockSupport warningGenerateLockSupport) {
        this.warningMapper = warningMapper;
        this.preWarningRuleMapper = preWarningRuleMapper;
        this.cropMapper = cropMapper;
        this.pestMapper = pestMapper;
        this.warningGenerateLockSupport = warningGenerateLockSupport;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public PageResult<WarningPageItemDTO> getWarningPage(WarningPageQueryDTO queryDTO) {
        try {
            WarningPageQueryDO queryDO = buildWarningPageQueryDO(queryDTO);

            Integer total = warningMapper.countByPage(queryDO);
            if (Objects.isNull(total) || total <= 0) {
                return new PageResult<>(0, new ArrayList<>());
            }

            List<WarningListItemDO> list = warningMapper.selectByPage(queryDO);
            List<WarningPageItemDTO> records = convertToWarningPageItemDTOList(list);

            return new PageResult<>(total, records);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("分页查询预警列表异常，queryDTO={}", queryDTO, e);
            throw new ServiceException(WarningErrorCode.WARNING_PAGE_QUERY_FAILED);
        }
    }

    @Override
    public WarningDetailDTO getWarningDetail(Long warningId) {
        try {
            WarningDetailDO detailDO = warningMapper.selectDetailById(warningId);
            if (Objects.isNull(detailDO)) {
                throw new ServiceException(WarningErrorCode.WARNING_NOT_EXIST);
            }
            return convertToWarningDetailDTO(detailDO);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询预警详情异常，warningId={}", warningId, e);
            throw new ServiceException(WarningErrorCode.WARNING_DETAIL_QUERY_FAILED);
        }
    }

    @Override
    public List<TodayWarningDTO> getTodayWarnings() {
        try {
            LocalDate today = LocalDate.now();
            List<WarningListItemDO> list = warningMapper.selectTodayWarnings(today);
            return convertToTodayWarningDTOList(list);
        } catch (Exception e) {
            log.error("查询当天预警列表异常", e);
            throw new ServiceException(WarningErrorCode.TODAY_WARNING_QUERY_FAILED);
        }
    }

    @Override
    public List<ForecastWarningDTO> getForecastWarnings(Integer days) {
        try {
            int validDays = (days == null ? DEFAULT_FORECAST_DAYS : days);
            if (validDays < 1 || validDays > 7) {
                throw new ServiceException(WarningErrorCode.GENERATE_DAYS_INVALID);
            }

            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(validDays);

            List<WarningListItemDO> list = warningMapper.selectForecastWarnings(startDate, endDate);
            return convertToForecastWarningDTOList(list);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询多天预警列表异常，days={}", days, e);
            throw new ServiceException(WarningErrorCode.FORECAST_WARNING_QUERY_FAILED);
        }
    }

    @Override
    public Boolean deleteWarning(Long warningId) {
        try {
            WarningDO warningDO = warningMapper.selectById(warningId);
            if (Objects.isNull(warningDO) || !Objects.equals(warningDO.getDeleteFlag(), 0)) {
                throw new ServiceException(WarningErrorCode.WARNING_NOT_EXIST);
            }

            int rows = warningMapper.deleteByIdLogical(warningId);
            if (rows != 1) {
                throw new ServiceException(WarningErrorCode.WARNING_DELETE_FAILED);
            }
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除预警异常，warningId={}", warningId, e);
            throw new ServiceException(WarningErrorCode.WARNING_DELETE_FAILED);
        }
    }

    @Override
    public Boolean batchDeleteWarnings(List<Long> warningIds) {
        try {
            if (CollectionUtils.isEmpty(warningIds)) {
                throw new ServiceException(WarningErrorCode.WARNING_IDS_EMPTY);
            }

            int rows = warningMapper.batchDeleteLogical(warningIds);
            if (rows <= 0) {
                throw new ServiceException(WarningErrorCode.WARNING_BATCH_DELETE_FAILED);
            }
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除预警异常，warningIds={}", warningIds, e);
            throw new ServiceException(WarningErrorCode.WARNING_BATCH_DELETE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningGenerateTodayResultDTO generateTodayWarnings() {
        if (!warningGenerateLockSupport.tryLock()) {
            throw new ServiceException(new ErrorCode(409, "WARNING_027", "预警生成任务正在执行，请稍后再试"));
        }

        try {
            LocalDate today = LocalDate.now();
            WeatherDailySummary dailySummary = getWeatherDailySummaryMap(today, today).get(today);

            if (Objects.isNull(dailySummary)) {
                throw new ServiceException(WarningErrorCode.WEATHER_QUERY_FAILED);
            }

            List<PreWarningRuleDO> enabledRules = getEnabledRules();
            if (CollectionUtils.isEmpty(enabledRules)) {
                WarningGenerateTodayResultDTO resultDTO = new WarningGenerateTodayResultDTO();
                resultDTO.setGeneratedCount(0);
                resultDTO.setSkippedCount(0);
                resultDTO.setWarningDate(today);
                return resultDTO;
            }

            int generatedCount = 0;
            int skippedCount = 0;

            for (PreWarningRuleDO ruleDO : enabledRules) {
                boolean matched = isRuleMatched(ruleDO, dailySummary);
                if (!matched) {
                    skippedCount++;
                    continue;
                }

                boolean generated = tryGenerateWarning(ruleDO, dailySummary, "TODAY");
                if (generated) {
                    generatedCount++;
                } else {
                    skippedCount++;
                }
            }

            WarningGenerateTodayResultDTO resultDTO = new WarningGenerateTodayResultDTO();
            resultDTO.setGeneratedCount(generatedCount);
            resultDTO.setSkippedCount(skippedCount);
            resultDTO.setWarningDate(today);
            return resultDTO;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("手动触发当天预警生成异常", e);
            throw new ServiceException(WarningErrorCode.WARNING_GENERATE_TODAY_FAILED);
        } finally {
            warningGenerateLockSupport.unlock();
        }
    }
    /**
     * 手动触发多天预警生成
     * 要求：
     * 1. 不清理旧预警
     * 2. 已存在的不重复生成
     * 3. 无启用规则时返回 200，0/0
     * 4. 规则异常时整批失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningGenerateForecastResultDTO generateForecastWarnings(Integer days) {
        if (!warningGenerateLockSupport.tryLock()) {
            throw new ServiceException(new ErrorCode(409, "WARNING_027", "预警生成任务正在执行，请稍后再试"));
        }

        try {
            if (Objects.isNull(days) || days < 1 || days > 7) {
                throw new ServiceException(WarningErrorCode.GENERATE_DAYS_INVALID);
            }

            LocalDate startDate = LocalDate.now().plusDays(1);
            LocalDate endDate = LocalDate.now().plusDays(days);

            Map<LocalDate, WeatherDailySummary> weatherMap = getWeatherDailySummaryMap(startDate, endDate);
            List<PreWarningRuleDO> enabledRules = getEnabledRules();

            if (CollectionUtils.isEmpty(enabledRules)) {
                WarningGenerateForecastResultDTO resultDTO = new WarningGenerateForecastResultDTO();
                resultDTO.setGeneratedCount(0);
                resultDTO.setSkippedCount(0);
                resultDTO.setDays(days);
                return resultDTO;
            }

            int generatedCount = 0;
            int skippedCount = 0;

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                WeatherDailySummary dailySummary = weatherMap.get(date);
                if (Objects.isNull(dailySummary)) {
                    continue;
                }

                for (PreWarningRuleDO ruleDO : enabledRules) {
                    boolean matched = isRuleMatched(ruleDO, dailySummary);
                    if (!matched) {
                        skippedCount++;
                        continue;
                    }

                    boolean generated = tryGenerateWarning(ruleDO, dailySummary, "FORECAST");
                    if (generated) {
                        generatedCount++;
                    } else {
                        skippedCount++;
                    }
                }
            }

            WarningGenerateForecastResultDTO resultDTO = new WarningGenerateForecastResultDTO();
            resultDTO.setGeneratedCount(generatedCount);
            resultDTO.setSkippedCount(skippedCount);
            resultDTO.setDays(days);
            return resultDTO;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("手动触发多天预警生成异常，days={}", days, e);
            throw new ServiceException(WarningErrorCode.WARNING_GENERATE_FORECAST_FAILED);
        } finally {
            warningGenerateLockSupport.unlock();
        }
    }
    /**
     * 定时任务刷新当天预警
     * 要求：
     * 1. 先清理今天旧 TODAY 预警
     * 2. 规则异常时跳过，不整批失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningGenerateTodayResultDTO refreshTodayWarningsForTask() {
        LocalDate today = LocalDate.now();

        warningMapper.deleteTodayWarningsByDate(today);

        WeatherDailySummary dailySummary = getWeatherDailySummaryMap(today, today).get(today);
        if (Objects.isNull(dailySummary)) {
            throw new ServiceException(WarningErrorCode.WEATHER_QUERY_FAILED);
        }

        List<PreWarningRuleDO> enabledRules = getEnabledRules();
        if (CollectionUtils.isEmpty(enabledRules)) {
            WarningGenerateTodayResultDTO resultDTO = new WarningGenerateTodayResultDTO();
            resultDTO.setGeneratedCount(0);
            resultDTO.setSkippedCount(0);
            resultDTO.setWarningDate(today);
            return resultDTO;
        }

        int generatedCount = 0;
        int skippedCount = 0;

        for (PreWarningRuleDO ruleDO : enabledRules) {
            try {
                boolean matched = isRuleMatched(ruleDO, dailySummary);

                if (!matched) {
                    skippedCount++;
                    continue;
                }

                boolean generated = tryGenerateWarning(ruleDO, dailySummary, "TODAY");
                if (generated) {
                    generatedCount++;
                } else {
                    skippedCount++;
                }
            } catch (ServiceException e) {
                log.error("定时当天预警生成跳过异常规则，ruleId={}, ruleName={}, reason={}",
                        ruleDO.getId(), ruleDO.getRuleName(), e.getMessage());
                skippedCount++;
            }
        }

        WarningGenerateTodayResultDTO resultDTO = new WarningGenerateTodayResultDTO();
        resultDTO.setGeneratedCount(generatedCount);
        resultDTO.setSkippedCount(skippedCount);
        resultDTO.setWarningDate(today);
        return resultDTO;
    }

    /**
     * 定时任务刷新多天预警
     * 要求：
     * 1. 先清理指定范围内旧 FORECAST 预警
     * 2. 规则异常时跳过，不整批失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WarningGenerateForecastResultDTO refreshForecastWarningsForTask(Integer days) {
        if (Objects.isNull(days) || days < 1 || days > 7) {
            throw new ServiceException(WarningErrorCode.GENERATE_DAYS_INVALID);
        }

        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(days);

        warningMapper.deleteForecastWarningsByDateRange(startDate, endDate);

        Map<LocalDate, WeatherDailySummary> weatherMap = getWeatherDailySummaryMap(startDate, endDate);
        List<PreWarningRuleDO> enabledRules = getEnabledRules();

        if (CollectionUtils.isEmpty(enabledRules)) {
            WarningGenerateForecastResultDTO resultDTO = new WarningGenerateForecastResultDTO();
            resultDTO.setGeneratedCount(0);
            resultDTO.setSkippedCount(0);
            resultDTO.setDays(days);
            return resultDTO;
        }

        int generatedCount = 0;
        int skippedCount = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            WeatherDailySummary dailySummary = weatherMap.get(date);
            if (Objects.isNull(dailySummary)) {
                continue;
            }

            for (PreWarningRuleDO ruleDO : enabledRules) {
                try {
                    boolean matched = isRuleMatched(ruleDO, dailySummary);

                    if (!matched) {
                        skippedCount++;
                        continue;
                    }

                    boolean generated = tryGenerateWarning(ruleDO, dailySummary, "FORECAST");
                    if (generated) {
                        generatedCount++;
                    } else {
                        skippedCount++;
                    }
                } catch (ServiceException e) {
                    log.error("定时多天预警生成跳过异常规则，ruleId={}, ruleName={}, reason={}",
                            ruleDO.getId(), ruleDO.getRuleName(), e.getMessage());
                    skippedCount++;
                }
            }
        }

        WarningGenerateForecastResultDTO resultDTO = new WarningGenerateForecastResultDTO();
        resultDTO.setGeneratedCount(generatedCount);
        resultDTO.setSkippedCount(skippedCount);
        resultDTO.setDays(days);
        return resultDTO;
    }

    // ==================== private 方法：分页转换 ====================

    private WarningPageQueryDO buildWarningPageQueryDO(WarningPageQueryDTO queryDTO) {
        WarningPageQueryDO queryDO = new WarningPageQueryDO();
        queryDO.setTitle(trimToNull(queryDTO.getTitle()));
        queryDO.setCropId(queryDTO.getCropId());
        queryDO.setPestId(queryDTO.getPestId());
        queryDO.setRiskLevel(trimToNull(queryDTO.getRiskLevel()));
        queryDO.setWarningType(trimToNull(queryDTO.getWarningType()));
        queryDO.setWarningDateStart(queryDTO.getWarningDateStart());
        queryDO.setWarningDateEnd(queryDTO.getWarningDateEnd());
        queryDO.setPageSize(queryDTO.getPageSize());
        queryDO.setOffset((queryDTO.getPageNum() - 1) * queryDTO.getPageSize());
        return queryDO;
    }

    private List<WarningPageItemDTO> convertToWarningPageItemDTOList(List<WarningListItemDO> list) {
        List<WarningPageItemDTO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }

        for (WarningListItemDO itemDO : list) {
            WarningPageItemDTO itemDTO = new WarningPageItemDTO();
            itemDTO.setWarningId(itemDO.getId());
            itemDTO.setTitle(itemDO.getTitle());
            itemDTO.setCropId(itemDO.getCropId());
            itemDTO.setCropName(itemDO.getCropName());
            itemDTO.setPestId(itemDO.getPestId());
            itemDTO.setPestName(itemDO.getPestName());
            itemDTO.setRiskLevel(itemDO.getRiskLevel());
            itemDTO.setWarningType(itemDO.getWarningType());
            itemDTO.setWarningDate(itemDO.getWarningDate());
            result.add(itemDTO);
        }
        return result;
    }

    private WarningDetailDTO convertToWarningDetailDTO(WarningDetailDO detailDO) {
        WarningDetailDTO detailDTO = new WarningDetailDTO();
        detailDTO.setWarningId(detailDO.getId());
        detailDTO.setTitle(detailDO.getTitle());
        detailDTO.setCropId(detailDO.getCropId());
        detailDTO.setCropName(detailDO.getCropName());
        detailDTO.setPestId(detailDO.getPestId());
        detailDTO.setPestName(detailDO.getPestName());
        detailDTO.setRiskLevel(detailDO.getRiskLevel());
        detailDTO.setWarningType(detailDO.getWarningType());
        detailDTO.setWarningDate(detailDO.getWarningDate());
        detailDTO.setRuleId(detailDO.getRuleId());
        detailDTO.setSuggestion(detailDO.getSuggestion());
        detailDTO.setGmtCreate(detailDO.getGmtCreate());
        return detailDTO;
    }

    private List<TodayWarningDTO> convertToTodayWarningDTOList(List<WarningListItemDO> list) {
        List<TodayWarningDTO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }

        for (WarningListItemDO itemDO : list) {
            TodayWarningDTO itemDTO = new TodayWarningDTO();
            itemDTO.setWarningId(itemDO.getId());
            itemDTO.setTitle(itemDO.getTitle());
            itemDTO.setCropId(itemDO.getCropId());
            itemDTO.setCropName(itemDO.getCropName());
            itemDTO.setPestId(itemDO.getPestId());
            itemDTO.setPestName(itemDO.getPestName());
            itemDTO.setRiskLevel(itemDO.getRiskLevel());
            itemDTO.setWarningType(itemDO.getWarningType());
            itemDTO.setWarningDate(itemDO.getWarningDate());
            result.add(itemDTO);
        }
        return result;
    }

    private List<ForecastWarningDTO> convertToForecastWarningDTOList(List<WarningListItemDO> list) {
        List<ForecastWarningDTO> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }

        for (WarningListItemDO itemDO : list) {
            ForecastWarningDTO itemDTO = new ForecastWarningDTO();
            itemDTO.setWarningId(itemDO.getId());
            itemDTO.setTitle(itemDO.getTitle());
            itemDTO.setCropId(itemDO.getCropId());
            itemDTO.setCropName(itemDO.getCropName());
            itemDTO.setPestId(itemDO.getPestId());
            itemDTO.setPestName(itemDO.getPestName());
            itemDTO.setRiskLevel(itemDO.getRiskLevel());
            itemDTO.setWarningType(itemDO.getWarningType());
            itemDTO.setWarningDate(itemDO.getWarningDate());
            result.add(itemDTO);
        }
        return result;
    }

    // ==================== private 方法：规则查询 ====================

    private List<PreWarningRuleDO> getEnabledRules() {
        try {
            LambdaQueryWrapper<PreWarningRuleDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PreWarningRuleDO::getDeleteFlag, 0)
                    .eq(PreWarningRuleDO::getRuleStatus, "ENABLED")
                    .orderByAsc(PreWarningRuleDO::getId);

            List<PreWarningRuleDO> rules = preWarningRuleMapper.selectList(queryWrapper);
            return CollectionUtils.isEmpty(rules) ? new ArrayList<>() : rules;
        } catch (Exception e) {
            log.error("查询启用预警规则异常", e);
            throw new ServiceException(WarningErrorCode.PREWARNING_RULE_QUERY_FAILED);
        }
    }

    // ==================== private 方法：天气查询与汇总 ====================

    private Map<LocalDate, WeatherDailySummary> getWeatherDailySummaryMap(LocalDate startDate, LocalDate endDate) {
        try {
            String url = buildOpenMeteoUrl(startDate, endDate);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            OpenMeteoForecastResponse responseObj =
                    objectMapper.readValue(response.body(), OpenMeteoForecastResponse.class);

            if (Objects.isNull(responseObj) || Objects.isNull(responseObj.getHourly())) {
                throw new ServiceException(WarningErrorCode.WEATHER_QUERY_FAILED);
            }

            return aggregateDailyWeather(responseObj.getHourly());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用Open-Meteo天气接口异常，startDate={}, endDate={}", startDate, endDate, e);
            throw new ServiceException(WarningErrorCode.WEATHER_QUERY_FAILED);
        }
    }

    private String buildOpenMeteoUrl(LocalDate startDate, LocalDate endDate) {
        return OPEN_METEO_FORECAST_URL
                + "?latitude=" + ZHANJIANG_LATITUDE
                + "&longitude=" + ZHANJIANG_LONGITUDE
                + "&timezone=auto"
                + "&start_date=" + startDate
                + "&end_date=" + endDate
                + "&hourly=" + OPEN_METEO_HOURLY_VARS;
    }

    private Map<LocalDate, WeatherDailySummary> aggregateDailyWeather(OpenMeteoHourly hourly) {
        List<String> timeList = hourly.getTime();
        List<BigDecimal> temperatureList = hourly.getTemperature2m();
        List<BigDecimal> humidityList = hourly.getRelativeHumidity2m();
        List<BigDecimal> precipitationList = hourly.getPrecipitation();
        List<BigDecimal> windSpeedList = hourly.getWindSpeed10m();

        if (CollectionUtils.isEmpty(timeList)) {
            return new LinkedHashMap<>();
        }

        Map<LocalDate, DailyAccumulator> accumulatorMap = new LinkedHashMap<>();

        for (int i = 0; i < timeList.size(); i++) {
            LocalDate date = LocalDate.parse(timeList.get(i).substring(0, 10));
            DailyAccumulator accumulator = accumulatorMap.computeIfAbsent(date, key -> new DailyAccumulator());

            if (temperatureList != null && i < temperatureList.size() && temperatureList.get(i) != null) {
                BigDecimal temp = temperatureList.get(i);

                if (accumulator.tempMin == null || temp.compareTo(accumulator.tempMin) < 0) {
                    accumulator.tempMin = temp;
                }

                if (accumulator.tempMax == null || temp.compareTo(accumulator.tempMax) > 0) {
                    accumulator.tempMax = temp;
                }
            }

            if (humidityList != null && i < humidityList.size() && humidityList.get(i) != null) {
                accumulator.humiditySum = accumulator.humiditySum.add(humidityList.get(i));
                accumulator.humidityCount++;
            }

            if (precipitationList != null && i < precipitationList.size() && precipitationList.get(i) != null) {
                accumulator.precipitationSum = accumulator.precipitationSum.add(precipitationList.get(i));
            }

            if (windSpeedList != null && i < windSpeedList.size() && windSpeedList.get(i) != null) {
                BigDecimal windSpeed = windSpeedList.get(i);

                if (accumulator.maxWindSpeed == null || windSpeed.compareTo(accumulator.maxWindSpeed) > 0) {
                    accumulator.maxWindSpeed = windSpeed;
                }
            }
        }

        Map<LocalDate, WeatherDailySummary> resultMap = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, DailyAccumulator> entry : accumulatorMap.entrySet()) {
            DailyAccumulator accumulator = entry.getValue();

            WeatherDailySummary summary = new WeatherDailySummary();
            summary.setDate(entry.getKey());
            summary.setTempMin(scale(accumulator.tempMin));
            summary.setTempMax(scale(accumulator.tempMax));
            summary.setAvgHumidity(calculateAverage(accumulator.humiditySum, accumulator.humidityCount));
            summary.setPrecipitation(scale(accumulator.precipitationSum));
            summary.setMaxWindSpeed(scale(accumulator.maxWindSpeed));

            resultMap.put(entry.getKey(), summary);
        }

        return resultMap;
    }

    private BigDecimal calculateAverage(BigDecimal sum, int count) {
        if (count <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        if (Objects.isNull(value)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== private 方法：规则匹配与生成 ====================

    private boolean isRuleMatched(PreWarningRuleDO ruleDO, WeatherDailySummary dailySummary) {
        if (Objects.isNull(ruleDO) || Objects.isNull(dailySummary)) {
            return false;
        }

        /**
         * 温度：使用当日最低温和最高温区间
         * min_temp -> 要求 tempMin >= minTemp
         * max_temp -> 要求 tempMax <= maxTemp
         */
        if (Objects.nonNull(ruleDO.getMinTemp())
                && dailySummary.getTempMin().compareTo(ruleDO.getMinTemp()) < 0) {
            return false;
        }

        if (Objects.nonNull(ruleDO.getMaxTemp())
                && dailySummary.getTempMax().compareTo(ruleDO.getMaxTemp()) > 0) {
            return false;
        }

        /**
         * 湿度：平均湿度
         */
        if (!isBetween(dailySummary.getAvgHumidity(), ruleDO.getMinHumidity(), ruleDO.getMaxHumidity())) {
            return false;
        }

        /**
         * 降雨量：当日累计降雨量
         */
        if (!isBetween(dailySummary.getPrecipitation(), ruleDO.getMinPrecipitation(), ruleDO.getMaxPrecipitation())) {
            return false;
        }

        /**
         * 风速：当日最大风速
         */
        if (!isBetween(dailySummary.getMaxWindSpeed(), ruleDO.getMinWindSpeed(), ruleDO.getMaxWindSpeed())) {
            return false;
        }

        return true;
    }

    private boolean isBetween(BigDecimal value, BigDecimal minValue, BigDecimal maxValue) {
        if (Objects.isNull(minValue) && Objects.isNull(maxValue)) {
            return true;
        }
        if (Objects.isNull(value)) {
            return false;
        }
        if (Objects.nonNull(minValue) && value.compareTo(minValue) < 0) {
            return false;
        }
        if (Objects.nonNull(maxValue) && value.compareTo(maxValue) > 0) {
            return false;
        }
        return true;
    }

    private boolean tryGenerateWarning(PreWarningRuleDO ruleDO,
                                       WeatherDailySummary dailySummary,
                                       String warningType) {
        WarningDO existWarning = warningMapper.selectByUniqueCondition(
                ruleDO.getCropId(),
                ruleDO.getPestId(),
                ruleDO.getId(),
                warningType,
                dailySummary.getDate()
        );
        if (Objects.nonNull(existWarning)) {
            return false;
        }

        CropDO cropDO = cropMapper.selectById(ruleDO.getCropId());
        if (Objects.isNull(cropDO) || !Objects.equals(cropDO.getDeleteFlag(), 0)) {
            String msg = String.format("预警规则【%s】(ID=%d)关联的作物不存在，请先修正规则后再重新生成",
                    ruleDO.getRuleName(), ruleDO.getId());
            throw new ServiceException(new ErrorCode(404, "WARNING_025", msg));
        }

        PestDO pestDO = pestMapper.selectById(ruleDO.getPestId());
        if (Objects.isNull(pestDO) || !Objects.equals(pestDO.getDeleteFlag(), 0)) {
            String msg = String.format("预警规则【%s】(ID=%d)关联的病虫害不存在，请先修正规则后再重新生成",
                    ruleDO.getRuleName(), ruleDO.getId());
            throw new ServiceException(new ErrorCode(404, "WARNING_026", msg));
        }

        WarningDO warningDO = new WarningDO();
        warningDO.setTitle(buildWarningTitle(cropDO.getName(), pestDO.getName(), ruleDO.getRiskLevel()));
        warningDO.setCropId(ruleDO.getCropId());
        warningDO.setPestId(ruleDO.getPestId());
        warningDO.setRiskLevel(ruleDO.getRiskLevel());
        warningDO.setWarningType(warningType);
        warningDO.setWarningDate(dailySummary.getDate());
        warningDO.setRuleId(ruleDO.getId());
        warningDO.setDeleteFlag(0);

        try {
            int rows = warningMapper.insertWarning(warningDO);
            if (rows != 1 || Objects.isNull(warningDO.getId())) {
                throw new ServiceException(WarningErrorCode.WARNING_CREATE_FAILED);
            }
            return true;
        } catch (DuplicateKeyException e) {
            log.warn("预警生成时唯一约束冲突，ruleId={}, cropId={}, pestId={}, warningType={}, warningDate={}",
                    ruleDO.getId(), ruleDO.getCropId(), ruleDO.getPestId(), warningType, dailySummary.getDate(), e);
            return false;
        }
    }

    private String buildWarningTitle(String cropName, String pestName, String riskLevel) {
        StringBuilder titleBuilder = new StringBuilder();

        if (cropName != null && !cropName.isBlank()) {
            titleBuilder.append(cropName);
        }
        if (pestName != null && !pestName.isBlank()) {
            titleBuilder.append(pestName);
        }
        if (riskLevel != null && !riskLevel.isBlank()) {
            titleBuilder.append(riskLevel).append("风险预警");
        } else {
            titleBuilder.append("预警");
        }

        return titleBuilder.toString();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // ==================== private static class ====================

    /**
     * Open-Meteo 响应对象
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenMeteoForecastResponse {

        private OpenMeteoHourly hourly;

        public OpenMeteoHourly getHourly() {
            return hourly;
        }

        public void setHourly(OpenMeteoHourly hourly) {
            this.hourly = hourly;
        }
    }

    /**
     * Open-Meteo 小时级数据
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenMeteoHourly {

        private List<String> time;

        @com.fasterxml.jackson.annotation.JsonProperty("temperature_2m")
        private List<BigDecimal> temperature2m;

        @com.fasterxml.jackson.annotation.JsonProperty("relative_humidity_2m")
        private List<BigDecimal> relativeHumidity2m;

        private List<BigDecimal> precipitation;

        @com.fasterxml.jackson.annotation.JsonProperty("wind_speed_10m")
        private List<BigDecimal> windSpeed10m;

        public List<String> getTime() {
            return time;
        }

        public void setTime(List<String> time) {
            this.time = time;
        }

        public List<BigDecimal> getTemperature2m() {
            return temperature2m;
        }

        public void setTemperature2m(List<BigDecimal> temperature2m) {
            this.temperature2m = temperature2m;
        }

        public List<BigDecimal> getRelativeHumidity2m() {
            return relativeHumidity2m;
        }

        public void setRelativeHumidity2m(List<BigDecimal> relativeHumidity2m) {
            this.relativeHumidity2m = relativeHumidity2m;
        }

        public List<BigDecimal> getPrecipitation() {
            return precipitation;
        }

        public void setPrecipitation(List<BigDecimal> precipitation) {
            this.precipitation = precipitation;
        }

        public List<BigDecimal> getWindSpeed10m() {
            return windSpeed10m;
        }

        public void setWindSpeed10m(List<BigDecimal> windSpeed10m) {
            this.windSpeed10m = windSpeed10m;
        }
    }

    /**
     * 天气日汇总
     */
    private static class WeatherDailySummary {

        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 当日最低温
         */
        private BigDecimal tempMin;

        /**
         * 当日最高温
         */
        private BigDecimal tempMax;

        /**
         * 平均湿度
         */
        private BigDecimal avgHumidity;

        /**
         * 当日累计降雨量
         */
        private BigDecimal precipitation;

        /**
         * 当日最大风速
         */
        private BigDecimal maxWindSpeed;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public BigDecimal getTempMin() {
            return tempMin;
        }

        public void setTempMin(BigDecimal tempMin) {
            this.tempMin = tempMin;
        }

        public BigDecimal getTempMax() {
            return tempMax;
        }

        public void setTempMax(BigDecimal tempMax) {
            this.tempMax = tempMax;
        }

        public BigDecimal getAvgHumidity() {
            return avgHumidity;
        }

        public void setAvgHumidity(BigDecimal avgHumidity) {
            this.avgHumidity = avgHumidity;
        }

        public BigDecimal getPrecipitation() {
            return precipitation;
        }

        public void setPrecipitation(BigDecimal precipitation) {
            this.precipitation = precipitation;
        }

        public BigDecimal getMaxWindSpeed() {
            return maxWindSpeed;
        }

        public void setMaxWindSpeed(BigDecimal maxWindSpeed) {
            this.maxWindSpeed = maxWindSpeed;
        }
    }

    /**
     * 日聚合累加器
     */
    private static class DailyAccumulator {

        private BigDecimal tempMin;
        private BigDecimal tempMax;

        private BigDecimal humiditySum = BigDecimal.ZERO;
        private int humidityCount = 0;

        private BigDecimal precipitationSum = BigDecimal.ZERO;

        private BigDecimal maxWindSpeed;
    }
}