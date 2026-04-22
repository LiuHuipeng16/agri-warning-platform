package com.zhku.agriwarningplatform.module.knowledgeqa.mapper.dataobject;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeqaCreateDO {
    /**
     * id
     */
    private Long id;
    /**
     * 问题
     */
    private String question;
    /**
     * 回答
     */
    private String answer;

    /**
     * 农作物id
     */
    private Long cropId;

    /**
     * 病虫害id
     */
    private Long pestId;
}
