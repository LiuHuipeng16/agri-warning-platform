package com.zhku.agriwarningplatform.module.knowledgeqa.mapper.dataobject;

import lombok.Data;

@Data
public class KnowledgeqaUptateDO {
    private Long id;
    private String question;
    private String answer;
    private Long cropId;
    private Long pestId;
}
