package com.zhku.agriwarningplatform.module.knowledgeqa.mapper.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeqaPageDO {
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
     * 作物id
     */
    private Long cropId;
    /**
     * 病害id
     */
    private Long pestId;
    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;
}
