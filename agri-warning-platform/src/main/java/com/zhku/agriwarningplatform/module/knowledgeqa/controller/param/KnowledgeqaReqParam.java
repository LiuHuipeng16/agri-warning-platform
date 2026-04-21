package com.zhku.agriwarningplatform.module.knowledgeqa.controller.param;

import com.zhku.agriwarningplatform.common.page.PageParam;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeqaReqParam extends PageParam {
    /**
     * 关键字
     */
    @Size(max = 50, message = "关键词长度不能超过50个字符")
    private String keyword;

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