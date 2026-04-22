package com.zhku.agriwarningplatform.module.knowledgeqa.controller.param;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-21
 * Time: 17:29
 */
@Data
public class KnowledgeqaUpdateParam {
    /**
     * 主键
     */
    @NotNull(message = "主键不能为空")
    @Min(value = 1, message = "主键ID必须大于0")
    private Long id;

    /**
     * 问题
     */
    @NotBlank(message = "问题内容不能为空")
    @Size(max = 200, message = "问题内容长度不能超过200个字符")
    private String question;

    /**
     * 回答
     */
    @NotBlank(message = "回答内容不能为空")
    @Size(max = 2000, message = "回答内容长度不能超过2000个字符")
    private String answer;

    /**
     * 农作物id
     */
    @Min(value = 1, message = "作物ID必须大于0")
    private Long cropId;

    /**
     * 病虫害id
     */
    @Min(value = 1, message = "病虫害ID必须大于0")
    private Long pestId;

}
