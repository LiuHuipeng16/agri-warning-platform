package com.zhku.agriwarningplatform.module.ai.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-07
 * Time: 20:24
 */
import lombok.Data;

@Data
public class AIWarningExplanationReqDTO {

    private Long warningId;

    private Long userId;

    private Boolean refresh;
}