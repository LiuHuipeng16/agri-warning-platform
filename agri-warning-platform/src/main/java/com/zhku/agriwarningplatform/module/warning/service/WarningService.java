package com.zhku.agriwarningplatform.module.warning.service;

import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.warning.service.dto.ForecastWarningDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.TodayWarningDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningDetailDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningGenerateForecastResultDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningGenerateTodayResultDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningPageItemDTO;
import com.zhku.agriwarningplatform.module.warning.service.dto.WarningPageQueryDTO;

import java.util.List;

/**
 * 预警 Service
 */
public interface WarningService {

    /**
     * 获取预警分页列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<WarningPageItemDTO> getWarningPage(WarningPageQueryDTO queryDTO);

    /**
     * 获取预警详情
     *
     * @param warningId 预警ID
     * @return 预警详情
     */
    WarningDetailDTO getWarningDetail(Long warningId);

    /**
     * 获取当天预警列表
     *
     * @return 当天预警列表
     */
    List<TodayWarningDTO> getTodayWarnings();

    /**
     * 获取多天预警列表
     *
     * @param days 天数
     * @return 多天预警列表
     */
    List<ForecastWarningDTO> getForecastWarnings(Integer days);

    /**
     * 删除预警
     *
     * @param warningId 预警ID
     * @return 是否删除成功
     */
    Boolean deleteWarning(Long warningId);

    /**
     * 批量删除预警
     *
     * @param warningIds 预警ID列表
     * @return 是否删除成功
     */
    Boolean batchDeleteWarnings(List<Long> warningIds);

    /**
     * 手动触发当天预警生成
     * 说明：
     * 1. 不清理旧预警
     * 2. 已存在的不重复生成
     * 3. 规则异常时整批失败
     *
     * @return 生成结果
     */
    WarningGenerateTodayResultDTO generateTodayWarnings();

    /**
     * 手动触发多天预警生成
     * 说明：
     * 1. 不清理旧预警
     * 2. 已存在的不重复生成
     * 3. 规则异常时整批失败
     *
     * @param days 生成天数
     * @return 生成结果
     */
    WarningGenerateForecastResultDTO generateForecastWarnings(Integer days);

    /**
     * 定时任务刷新当天预警
     * 说明：
     * 1. 先清理当天旧的 TODAY 预警
     * 2. 再按最新天气重算
     * 3. 规则异常时跳过，不整批失败
     *
     * @return 生成结果
     */
    WarningGenerateTodayResultDTO refreshTodayWarningsForTask();

    /**
     * 定时任务刷新多天预警
     * 说明：
     * 1. 先清理未来范围内旧的 FORECAST 预警
     * 2. 再按最新天气重算
     * 3. 规则异常时跳过，不整批失败
     *
     * @param days 生成天数
     * @return 生成结果
     */
    WarningGenerateForecastResultDTO refreshForecastWarningsForTask(Integer days);
}