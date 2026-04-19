package com.zhku.agriwarningplatform.module.ai.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:28
 */
import lombok.Data;

@Data
public class AIWarningSuggestionReqDTO {

    /**
     * 预警ID
     */
    private Long warningId;

    /**
     * 用户ID
     */
    private Long userId;
}