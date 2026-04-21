package com.zhku.agriwarningplatform.module.knowledgeqa.controller.vo;

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
    private Long cropId;

    /**
     * 病害id
     */
    private Long pestId;

    /**
     * 创建时间
     */
    private String gmtCreate;
}