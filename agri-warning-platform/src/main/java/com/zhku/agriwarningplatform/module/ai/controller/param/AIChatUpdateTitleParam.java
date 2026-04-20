package com.zhku.agriwarningplatform.module.ai.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:30
 */
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class AIChatUpdateTitleParam {

    /**
     * 会话ID
     */
    @Size(max = 64, message = "chatId长度不能超过64")
    @NotBlank(message = "会话ID不能为空")
    private String chatId;

    /**
     * 新标题
     */
    @Size(max = 200, message = "title长度不能超过200")
    @NotBlank(message = "会话标题不能为空")
    private String title;
}