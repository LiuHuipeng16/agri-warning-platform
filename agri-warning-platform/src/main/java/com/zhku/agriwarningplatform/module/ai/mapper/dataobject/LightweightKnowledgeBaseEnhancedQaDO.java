package com.zhku.agriwarningplatform.module.ai.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:24
 */
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LightweightKnowledgeBaseEnhancedQaDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 作物ID
     */
    private Long cropId;

    /**
     * 病虫害ID
     */
    private Long pestId;

    /**
     * 问题
     */
    private String question;

    /**
     * 答案
     */
    private String answer;

    /**
     * 删除标记：0未删除，1已删除
     */
    private Integer deleteFlag;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreate;

    /**
     * 修改时间
     */
    private LocalDateTime gmtModified;
}