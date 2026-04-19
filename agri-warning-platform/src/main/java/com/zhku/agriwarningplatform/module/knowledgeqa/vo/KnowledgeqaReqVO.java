package com.zhku.agriwarningplatform.module.knowledgeqa.vo;

import com.zhku.agriwarningplatform.common.page.PageParam;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeqaReqVO extends PageParam {
    /**
     * 主键
     */
    @Min(value = 1, message = "主键ID必须大于0")
    private Long id ;
    /**
     * 关键字
     */
    @Size(max = 50, message = "关键词长度不能超过50个字符")
    private String keyword ;
    /**
     * 农作物id
     */
    @Min(value = 1, message = "作物ID必须大于0")
    private String cropId ;
    /**
     * 病害id
     */
    @Min(value = 1, message = "病虫害ID必须大于0")
    private String pestId ;
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
}
