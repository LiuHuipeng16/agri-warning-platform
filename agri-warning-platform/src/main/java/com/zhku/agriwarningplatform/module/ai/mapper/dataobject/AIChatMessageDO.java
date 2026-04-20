package com.zhku.agriwarningplatform.module.ai.mapper.dataobject;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AIChatMessageDO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息角色：user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息状态：STREAMING / COMPLETED / STOPPED / FAILED
     */
    private String messageStatus;

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