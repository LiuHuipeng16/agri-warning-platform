package com.zhku.agriwarningplatform.module.ai.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:25
 */
import lombok.Data;

@Data
public class AIAssistantChatReqDTO {

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * 用户问题
     */
    private String prompt;

    /**
     * 上下文类型：CROP / PEST / WARNING / NONE
     */
    private String contextType;

    /**
     * 上下文ID
     */
    private Long contextId;

    /**
     * 用户ID
     */
    private Long userId;
}
