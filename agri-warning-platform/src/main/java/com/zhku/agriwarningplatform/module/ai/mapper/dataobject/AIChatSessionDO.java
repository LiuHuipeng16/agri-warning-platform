package com.zhku.agriwarningplatform.module.ai.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-18
 * Time: 22:22
 */
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AIChatSessionDO {

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话类型：ASSISTANT / CHAT
     */
    private String sessionType;

    /**
     * 上下文类型：CROP / PEST / WARNING / NONE
     */
    private String contextType;

    /**
     * 上下文业务ID
     */
    private Long contextId;

    /**
     * 会话标题
     */
    private String title;

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
