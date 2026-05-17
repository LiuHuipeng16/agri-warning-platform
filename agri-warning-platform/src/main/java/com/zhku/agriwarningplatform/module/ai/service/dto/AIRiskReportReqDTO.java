package com.zhku.agriwarningplatform.module.ai.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-07
 * Time: 20:25
 */
import lombok.Data;

@Data
public class AIRiskReportReqDTO {

    private Integer days;

    private Boolean refresh;

    private Long userId;

    private String role;
    /**
     * 原始days字符串
     */
    private String daysStr;

    /**
     * 原始refresh字符串
     */
    private String refreshStr;
}
