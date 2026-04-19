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
public class AIChatMessageDTO {

    /**
     * 消息角色：user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;
}
