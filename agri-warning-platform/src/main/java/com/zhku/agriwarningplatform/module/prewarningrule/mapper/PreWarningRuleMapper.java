package com.zhku.agriwarningplatform.module.prewarningrule.mapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 11:19
 */

import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleOptionParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRulePageParam;
import com.zhku.agriwarningplatform.module.prewarningrule.mapper.dataobject.PreWarningRuleDO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRuleDTO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRuleOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PreWarningRuleMapper {

    /**
     * 分页总数
     *
     * @param param 查询参数
     * @return 总数
     */
    Integer countPage(@Param("param") PreWarningRulePageParam param);

    /**
     * 分页列表
     *
     * @param param 查询参数
     * @return 列表
     */
    List<PreWarningRuleDTO> selectPage(@Param("param") PreWarningRulePageParam param);

    /**
     * 根据ID查询规则DO
     *
     * @param id 规则ID
     * @return 规则DO
     */
    PreWarningRuleDO selectById(@Param("id") Long id);

    /**
     * 根据ID查询详情
     *
     * @param id 规则ID
     * @return 规则详情
     */
    PreWarningRuleDTO selectDetailById(@Param("id") Long id);

    /**
     * 新增规则
     *
     * @param preWarningRuleDO 规则DO
     * @return 影响行数
     */
    int insert(PreWarningRuleDO preWarningRuleDO);

    /**
     * 部分更新规则
     *
     * @param preWarningRuleDO 规则DO
     * @return 影响行数
     */
    int updateByIdSelective(PreWarningRuleDO preWarningRuleDO);

    /**
     * 逻辑删除
     *
     * @param id 规则ID
     * @return 影响行数
     */
    int logicalDeleteById(@Param("id") Long id);

    /**
     * 更新规则状态
     *
     * @param id         规则ID
     * @param ruleStatus 规则状态
     * @return 影响行数
     */
    int updateRuleStatusById(@Param("id") Long id, @Param("ruleStatus") String ruleStatus);

    /**
     * 查询下拉选项
     *
     * @param param 查询参数
     * @return 下拉选项
     */
    List<PreWarningRuleOptionDTO> selectOptions(@Param("param") PreWarningRuleOptionParam param);
}