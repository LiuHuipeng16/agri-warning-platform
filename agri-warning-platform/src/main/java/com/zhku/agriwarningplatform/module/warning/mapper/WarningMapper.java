package com.zhku.agriwarningplatform.module.warning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningDO;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningDetailDO;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningListItemDO;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningPageQueryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 预警 Mapper
 */
@Mapper
public interface WarningMapper extends BaseMapper<WarningDO> {

    /**
     * 分页总数查询
     *
     * @param queryDO 查询条件
     * @return 总条数
     */
    Integer countByPage(@Param("query") WarningPageQueryDO queryDO);

    /**
     * 分页列表查询
     *
     * @param queryDO 查询条件
     * @return 分页列表
     */
    List<WarningListItemDO> selectByPage(@Param("query") WarningPageQueryDO queryDO);

    /**
     * 根据预警ID查询详情
     *
     * @param id 预警ID
     * @return 预警详情
     */
    WarningDetailDO selectDetailById(@Param("id") Long id);

    /**
     * 查询当天预警列表
     *
     * @param warningDate 预警日期
     * @return 当天预警列表
     */
    List<WarningListItemDO> selectTodayWarnings(@Param("warningDate") LocalDate warningDate);

    /**
     * 查询未来多天预警列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 多天预警列表
     */
    List<WarningListItemDO> selectForecastWarnings(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 根据预警ID逻辑删除
     *
     * @param id 预警ID
     * @return 影响行数
     */
    int deleteByIdLogical(@Param("id") Long id);

    /**
     * 批量逻辑删除
     *
     * @param warningIds 预警ID列表
     * @return 影响行数
     */
    int batchDeleteLogical(@Param("warningIds") List<Long> warningIds);

    /**
     * 根据唯一业务条件查询是否已存在
     *
     * @param cropId      作物ID
     * @param pestId      病虫害ID
     * @param ruleId      规则ID
     * @param warningType 预警类型
     * @param warningDate 预警日期
     * @return 预警记录
     */
    WarningDO selectByUniqueCondition(@Param("cropId") Long cropId,
                                      @Param("pestId") Long pestId,
                                      @Param("ruleId") Long ruleId,
                                      @Param("warningType") String warningType,
                                      @Param("warningDate") LocalDate warningDate);

    /**
     * 插入预警
     *
     * @param warningDO 预警DO
     * @return 影响行数
     */
    int insertWarning(WarningDO warningDO);

    /**
     * 定时任务刷新时：逻辑删除指定日期的当天预警
     *
     * @param warningDate 预警日期
     * @return 影响行数
     */
    int deleteTodayWarningsByDate(@Param("warningDate") LocalDate warningDate);

    /**
     * 定时任务刷新时：逻辑删除指定日期范围内的多天预警
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 影响行数
     */
    int deleteForecastWarningsByDateRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
}