package com.zhku.agriwarningplatform.module.ai.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:29
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AIAssistantChatStreamParam {

    /**
     * 当前会话ID
     */
    @Size(max = 64, message = "chatId长度不能超过64")
    @NotBlank(message = "会话ID不能为空")
    private String chatId;

    /**
     * 用户当前输入的问题内容
     */
    @NotBlank(message = "提问内容不能为空")
    private String prompt;

    /**
     * 当前页面上下文类型：CROP / PEST / WARNING / NONE
     */
    private String contextType;

    /**
     * 当前页面业务对象ID
     */
    private Long contextId;
}