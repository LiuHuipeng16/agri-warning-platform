package com.zhku.agriwarningplatform.module.prewarningrule.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 14:58
 */

import lombok.Data;

import java.util.List;

@Data
public class PreWarningRulePageDTO {

    /**
     * 总数
     */
    private Integer total;

    /**
     * 记录列表
     */
    private List<PreWarningRuleDTO> records;
}