package com.zhku.agriwarningplatform.module.ai.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:28
 */
import lombok.Data;

@Data
public class AIKnowledgeDocumentDTO {

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 文档文本
     */
    private String content;

    /**
     * 作物ID
     */
    private Long cropId;

    /**
     * 作物名称
     */
    private String cropName;

    /**
     * 病虫害ID
     */
    private Long pestId;

    /**
     * 病虫害名称
     */
    private String pestName;
}
