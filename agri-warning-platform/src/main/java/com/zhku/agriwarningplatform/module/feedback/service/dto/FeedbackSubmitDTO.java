package com.zhku.agriwarningplatform.module.feedback.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:30
 */
import lombok.Data;

/**
 * 提交反馈 DTO
 */
@Data
public class FeedbackSubmitDTO {

    /**
     * 反馈用户ID
     */
    private Long userId;

    /**
     * 反馈目标类型：WARNING / AI_IMAGE / AI_CHAT
     */
    private String targetType;

    /**
     * 反馈目标ID
     */
    private String targetId;

    /**
     * 反馈结果：YES / NO / UNCERTAIN
     */
    private String feedbackResult;

    /**
     * 反馈补充说明
     */
    private String content;
}
