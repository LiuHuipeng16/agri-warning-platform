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
public class AIAssistantHistoryParam {

    /**
     * 当前会话ID
     */
    @Size(max = 64, message = "chatId长度不能超过64")
    @NotBlank(message = "会话ID不能为空")
    private String chatId;
}