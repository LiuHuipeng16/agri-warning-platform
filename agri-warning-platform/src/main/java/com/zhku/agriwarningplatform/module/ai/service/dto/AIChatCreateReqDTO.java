package com.zhku.agriwarningplatform.module.ai.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:26
 */
import lombok.Data;

@Data
public class AIChatCreateReqDTO {

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 用户ID
     */
    private Long userId;
}
