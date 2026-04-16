package com.zhku.agriwarningplatform.module.knowledgeqa.vo;

import lombok.Data;

@Data
public class KnowledgeqaRespVO {
    /**
     * 主键
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
    private String cropId;
    /**
     * 病害id
     */
    private String pestId;
    /**
     * 创建时间
     */
    private String gmtCreate;
}
