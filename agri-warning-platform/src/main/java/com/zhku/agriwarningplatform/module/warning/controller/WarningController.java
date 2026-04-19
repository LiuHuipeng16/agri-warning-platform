package com.zhku.agriwarningplatform.module.warning.controller;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 3:34
 */
import com.zhku.agriwarningplatform.common.errorcode.WarningErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.common.util.JacksonUtils;
import com.zhku.agriwarningplatform.module.warning.controller.param.WarningBatchDeleteParam;
import com.zhku.agriwarningplatform.module.warning.controller.param.WarningGenerateForecastParam;
import com.zhku.agriwarningplatform.module.warning.controller.param.WarningPageParam;
import com.zhku.agriwarningplatform.module.warning.controller.vo.*;
import com.zhku.agriwarningplatform.module.warning.service.WarningService;
import com.zhku.agriwarningplatform.module.warning.service.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 预警 Controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/warnings")
public class WarningController {

    private static final int DEFAULT_FORECAST_DAYS = 4;

    private final WarningService warningService;

    public WarningController(WarningService warningService) {
        this.warningService = warningService;
    }

    /**
     * 获取预警分页列表
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    public CommonResult<PageResult<WarningPageItemVO>> getWarningPage(@Valid @ModelAttribute WarningPageParam param) {
        log.info("进入接口:WarningController#getWarningPage,param={}", JacksonUtils.writeValueAsString(param));

        validateWarningPageParam(param);

        WarningPageQueryDTO queryDTO = convertToWarningPageQueryDTO(param);
        PageResult<WarningPageItemDTO> pageResult = warningService.getWarningPage(queryDTO);
        PageResult<WarningPageItemVO> resultVO = convertToWarningPageVO(pageResult);

        return CommonResult.success(resultVO);
    }

    /**
     * 获取预警详情
     *
     * @param warningId 预警ID
     * @return 预警详情
     */
    @GetMapping("/detail/{warningId}")
    public CommonResult<WarningDetailVO> getWarningDetail(
            @PathVariable("warningId")
            @Min(value = 1, message = "预警ID必须大于等于1") Long warningId) {
        log.info("进入接口:WarningController#getWarningDetail,warningId={}", warningId);

        if (Objects.isNull(warningId) || warningId < 1) {
            throw new ControllerException(WarningErrorCode.WARNING_ID_INVALID);
        }

        WarningDetailDTO detailDTO = warningService.getWarningDetail(warningId);
        WarningDetailVO detailVO = convertToWarningDetailVO(detailDTO);

        return CommonResult.success(detailVO);
    }

    /**
     * 获取当天预警列表
     *
     * @return 当天预警列表
     */
    @GetMapping("/today")
    public CommonResult<List<TodayWarningVO>> getTodayWarnings() {
        log.info("进入接口:WarningController#getTodayWarnings");

        List<TodayWarningDTO> dtoList = warningService.getTodayWarnings();
        List<TodayWarningVO> voList = convertToTodayWarningVOList(dtoList);

        return CommonResult.success(voList);
    }

    /**
     * 获取多天预警列表
     *
     * @param days 天数，默认4，范围1~7
     * @return 多天预警列表
     */
    @GetMapping("/forecast")
    public CommonResult<List<ForecastWarningVO>> getForecastWarnings(
            @RequestParam(value = "days", required = false) Integer days) {
        log.info("进入接口:WarningController#getForecastWarnings,days={}", days);

        Integer validDays = days;
        if (Objects.isNull(validDays)) {
            validDays = DEFAULT_FORECAST_DAYS;
        }

        if (validDays < 1 || validDays > 7) {
            throw new ControllerException(WarningErrorCode.GENERATE_DAYS_INVALID);
        }

        List<ForecastWarningDTO> dtoList = warningService.getForecastWarnings(validDays);
        List<ForecastWarningVO> voList = convertToForecastWarningVOList(dtoList);

        return CommonResult.success(voList);
    }

    /**
     * 删除预警
     *
     * @param warningId 预警ID
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{warningId}")
    public CommonResult<Boolean> deleteWarning(
            @PathVariable("warningId")
            @Min(value = 1, message = "预警ID必须大于等于1") Long warningId) {
        log.info("进入接口:WarningController#deleteWarning,warningId={}", warningId);

        if (Objects.isNull(warningId) || warningId < 1) {
            throw new ControllerException(WarningErrorCode.WARNING_ID_INVALID);
        }

        Boolean result = warningService.deleteWarning(warningId);
        return CommonResult.success(result);
    }

    /**
     * 批量删除预警
     *
     * @param param 批量删除参数
     * @return 是否删除成功
     */
    @DeleteMapping("/batchDelete")
    public CommonResult<Boolean> batchDeleteWarnings(@Valid @RequestBody WarningBatchDeleteParam param) {
        log.info("进入接口:WarningController#batchDeleteWarnings,param={}", JacksonUtils.writeValueAsString(param));

        validateWarningBatchDeleteParam(param);

        Boolean result = warningService.batchDeleteWarnings(param.getWarningIds());
        return CommonResult.success(result);
    }

    /**
     * 手动触发当天预警生成
     *
     * @return 生成结果
     */
    @PostMapping("/generateToday")
    public CommonResult<WarningGenerateTodayResultVO> generateTodayWarnings() {
        log.info("进入接口:WarningController#generateTodayWarnings");

        WarningGenerateTodayResultDTO resultDTO = warningService.generateTodayWarnings();
        WarningGenerateTodayResultVO resultVO = convertToWarningGenerateTodayResultVO(resultDTO);

        return CommonResult.success(resultVO);
    }

    /**
     * 手动触发多天预警生成
     *
     * @param param 生成参数
     * @return 生成结果
     */
    @PostMapping("/generateForecast")
    public CommonResult<WarningGenerateForecastResultVO> generateForecastWarnings(
            @Valid @RequestBody WarningGenerateForecastParam param) {
        log.info("进入接口:WarningController#generateForecastWarnings,param={}", JacksonUtils.writeValueAsString(param));

        if (Objects.isNull(param) || Objects.isNull(param.getDays())) {
            throw new ControllerException(WarningErrorCode.GENERATE_DAYS_INVALID);
        }

        if (param.getDays() < 1 || param.getDays() > 7) {
            throw new ControllerException(WarningErrorCode.GENERATE_DAYS_INVALID);
        }

        WarningGenerateForecastResultDTO resultDTO = warningService.generateForecastWarnings(param.getDays());
        WarningGenerateForecastResultVO resultVO = convertToWarningGenerateForecastResultVO(resultDTO);

        return CommonResult.success(resultVO);
    }

    // ==================== private 校验方法 ====================

    private void validateWarningPageParam(WarningPageParam param) {
        if (Objects.isNull(param)) {
            throw new ControllerException(WarningErrorCode.PAGE_PARAM_INVALID);
        }

        if (Objects.isNull(param.getPageNum()) || param.getPageNum() < 1) {
            throw new ControllerException(WarningErrorCode.PAGE_PARAM_INVALID);
        }

        if (Objects.isNull(param.getPageSize()) || param.getPageSize() < 1 || param.getPageSize() > 50) {
            throw new ControllerException(WarningErrorCode.PAGE_PARAM_INVALID);
        }

        if (param.getRiskLevel() != null
                && !param.getRiskLevel().isBlank()
                && !isValidRiskLevel(param.getRiskLevel())) {
            throw new ControllerException(WarningErrorCode.RISK_LEVEL_INVALID);
        }

        if (param.getWarningType() != null
                && !param.getWarningType().isBlank()
                && !isValidWarningType(param.getWarningType())) {
            throw new ControllerException(WarningErrorCode.WARNING_TYPE_INVALID);
        }

        if (param.getWarningDateStart() != null
                && param.getWarningDateEnd() != null
                && param.getWarningDateStart().isAfter(param.getWarningDateEnd())) {
            throw new ControllerException(WarningErrorCode.WARNING_DATE_RANGE_INVALID);
        }
    }

    private void validateWarningBatchDeleteParam(WarningBatchDeleteParam param) {
        if (Objects.isNull(param)
                || Objects.isNull(param.getWarningIds())
                || param.getWarningIds().isEmpty()) {
            throw new ControllerException(WarningErrorCode.WARNING_IDS_EMPTY);
        }

        if (param.getWarningIds().size() > 50) {
            throw new ControllerException(WarningErrorCode.WARNING_IDS_TOO_MANY);
        }

        for (Long warningId : param.getWarningIds()) {
            if (Objects.isNull(warningId) || warningId < 1) {
                throw new ControllerException(WarningErrorCode.WARNING_ID_INVALID);
            }
        }
    }

    private boolean isValidRiskLevel(String riskLevel) {
        return "低".equals(riskLevel) || "中".equals(riskLevel) || "高".equals(riskLevel);
    }

    private boolean isValidWarningType(String warningType) {
        return "TODAY".equals(warningType) || "FORECAST".equals(warningType);
    }

    // ==================== private 转换方法 ====================

    private WarningPageQueryDTO convertToWarningPageQueryDTO(WarningPageParam param) {
        WarningPageQueryDTO queryDTO = new WarningPageQueryDTO();
        queryDTO.setPageNum(param.getPageNum());
        queryDTO.setPageSize(param.getPageSize());
        queryDTO.setTitle(trimToNull(param.getTitle()));
        queryDTO.setCropId(param.getCropId());
        queryDTO.setPestId(param.getPestId());
        queryDTO.setRiskLevel(trimToNull(param.getRiskLevel()));
        queryDTO.setWarningType(trimToNull(param.getWarningType()));
        queryDTO.setWarningDateStart(param.getWarningDateStart());
        queryDTO.setWarningDateEnd(param.getWarningDateEnd());
        return queryDTO;
    }

    private PageResult<WarningPageItemVO> convertToWarningPageVO(PageResult<WarningPageItemDTO> pageResult) {
        List<WarningPageItemVO> records = new ArrayList<>();
        if (pageResult != null && pageResult.getRecords() != null) {
            for (WarningPageItemDTO itemDTO : pageResult.getRecords()) {
                WarningPageItemVO itemVO = new WarningPageItemVO();
                itemVO.setWarningId(itemDTO.getWarningId());
                itemVO.setTitle(itemDTO.getTitle());
                itemVO.setCropId(itemDTO.getCropId());
                itemVO.setCropName(itemDTO.getCropName());
                itemVO.setPestId(itemDTO.getPestId());
                itemVO.setPestName(itemDTO.getPestName());
                itemVO.setRiskLevel(itemDTO.getRiskLevel());
                itemVO.setWarningType(itemDTO.getWarningType());
                itemVO.setWarningDate(itemDTO.getWarningDate());
                records.add(itemVO);
            }
        }

        Integer total = pageResult == null || pageResult.getTotal() == null ? 0 : pageResult.getTotal();
        return new PageResult<>(total, records);
    }

    private WarningDetailVO convertToWarningDetailVO(WarningDetailDTO detailDTO) {
        WarningDetailVO detailVO = new WarningDetailVO();
        detailVO.setWarningId(detailDTO.getWarningId());
        detailVO.setTitle(detailDTO.getTitle());
        detailVO.setCropId(detailDTO.getCropId());
        detailVO.setCropName(detailDTO.getCropName());
        detailVO.setPestId(detailDTO.getPestId());
        detailVO.setPestName(detailDTO.getPestName());
        detailVO.setRiskLevel(detailDTO.getRiskLevel());
        detailVO.setWarningType(detailDTO.getWarningType());
        detailVO.setWarningDate(detailDTO.getWarningDate());
        detailVO.setRuleId(detailDTO.getRuleId());
        detailVO.setSuggestion(detailDTO.getSuggestion());
        detailVO.setGmtCreate(detailDTO.getGmtCreate());
        return detailVO;
    }

    private List<TodayWarningVO> convertToTodayWarningVOList(List<TodayWarningDTO> dtoList) {
        List<TodayWarningVO> voList = new ArrayList<>();
        if (dtoList == null || dtoList.isEmpty()) {
            return voList;
        }

        for (TodayWarningDTO dto : dtoList) {
            TodayWarningVO vo = new TodayWarningVO();
            vo.setWarningId(dto.getWarningId());
            vo.setTitle(dto.getTitle());
            vo.setCropId(dto.getCropId());
            vo.setCropName(dto.getCropName());
            vo.setPestId(dto.getPestId());
            vo.setPestName(dto.getPestName());
            vo.setRiskLevel(dto.getRiskLevel());
            vo.setWarningType(dto.getWarningType());
            vo.setWarningDate(dto.getWarningDate());
            voList.add(vo);
        }
        return voList;
    }

    private List<ForecastWarningVO> convertToForecastWarningVOList(List<ForecastWarningDTO> dtoList) {
        List<ForecastWarningVO> voList = new ArrayList<>();
        if (dtoList == null || dtoList.isEmpty()) {
            return voList;
        }

        for (ForecastWarningDTO dto : dtoList) {
            ForecastWarningVO vo = new ForecastWarningVO();
            vo.setWarningId(dto.getWarningId());
            vo.setTitle(dto.getTitle());
            vo.setCropId(dto.getCropId());
            vo.setCropName(dto.getCropName());
            vo.setPestId(dto.getPestId());
            vo.setPestName(dto.getPestName());
            vo.setRiskLevel(dto.getRiskLevel());
            vo.setWarningType(dto.getWarningType());
            vo.setWarningDate(dto.getWarningDate());
            voList.add(vo);
        }
        return voList;
    }

    private WarningGenerateTodayResultVO convertToWarningGenerateTodayResultVO(
            WarningGenerateTodayResultDTO resultDTO) {
        WarningGenerateTodayResultVO resultVO = new WarningGenerateTodayResultVO();
        resultVO.setGeneratedCount(resultDTO.getGeneratedCount());
        resultVO.setSkippedCount(resultDTO.getSkippedCount());
        resultVO.setWarningDate(resultDTO.getWarningDate());
        return resultVO;
    }

    private WarningGenerateForecastResultVO convertToWarningGenerateForecastResultVO(
            WarningGenerateForecastResultDTO resultDTO) {
        WarningGenerateForecastResultVO resultVO = new WarningGenerateForecastResultVO();
        resultVO.setGeneratedCount(resultDTO.getGeneratedCount());
        resultVO.setSkippedCount(resultDTO.getSkippedCount());
        resultVO.setDays(resultDTO.getDays());
        return resultVO;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}