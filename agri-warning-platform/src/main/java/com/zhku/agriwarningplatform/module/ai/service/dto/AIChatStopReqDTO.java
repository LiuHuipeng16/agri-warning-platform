package com.zhku.agriwarningplatform.module.ai.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:27
 */
import lombok.Data;

@Data
public class AIChatStopReqDTO {

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * 用户ID
     */
    private Long userId;
}
