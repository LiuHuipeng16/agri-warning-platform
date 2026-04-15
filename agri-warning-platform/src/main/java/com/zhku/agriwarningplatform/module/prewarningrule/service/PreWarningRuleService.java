package com.zhku.agriwarningplatform.module.prewarningrule.service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 15:54
 */

import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleChangeStatusParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleCreateParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleOptionParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRulePageParam;
import com.zhku.agriwarningplatform.module.prewarningrule.controller.param.PreWarningRuleUpdateParam;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRuleDTO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRuleOptionDTO;
import com.zhku.agriwarningplatform.module.prewarningrule.service.dto.PreWarningRulePageDTO;

import java.util.List;

public interface PreWarningRuleService {

    /**
     * 分页查询预警规则
     *
     * @param param 查询参数
     * @return 分页结果
     */
    PreWarningRulePageDTO page(PreWarningRulePageParam param);

    /**
     * 查询预警规则详情
     *
     * @param ruleId 规则ID
     * @return 规则详情
     */
    PreWarningRuleDTO detail(Long ruleId);

    /**
     * 新增预警规则
     *
     * @param param 新增参数
     * @return 新增后的规则ID
     */
    Long create(PreWarningRuleCreateParam param);

    /**
     * 编辑预警规则
     *
     * @param param 编辑参数
     * @return 是否成功
     */
    Boolean update(PreWarningRuleUpdateParam param);

    /**
     * 删除预警规则
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    Boolean delete(Long ruleId);

    /**
     * 修改规则状态
     *
     * @param param 状态参数
     * @return 是否成功
     */
    Boolean changeStatus(PreWarningRuleChangeStatusParam param);

    /**
     * 获取规则下拉选项
     *
     * @param param 查询参数
     * @return 下拉选项
     */
    List<PreWarningRuleOptionDTO> options(PreWarningRuleOptionParam param);
}