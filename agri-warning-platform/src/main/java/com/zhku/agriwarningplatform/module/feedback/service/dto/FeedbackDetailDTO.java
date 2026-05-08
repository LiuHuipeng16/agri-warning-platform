package com.zhku.agriwarningplatform.module.feedback.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:32
 */
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈详情 DTO
 */
@Data
public class FeedbackDetailDTO {

    /**
     * 反馈ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 反馈目标类型
     */
    private String targetType;

    /**
     * 反馈目标ID
     */
    private String targetId;

    /**
     * 反馈结果
     */
    private String feedbackResult;

    /**
     * 反馈补充说明
     */
    private String content;

    /**
     * 反馈目标摘要信息
     */
    private Object targetDetail;

    /**
     * 反馈时间
     */
    private LocalDateTime gmtCreate;
}
