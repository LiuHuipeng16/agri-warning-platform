package com.zhku.agriwarningplatform.module.feedback.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:32
 */
import lombok.Data;

/**
 * 编辑反馈 DTO
 */
@Data
public class FeedbackUpdateDTO {

    /**
     * 反馈ID
     */
    private Long id;

    /**
     * 当前登录用户ID
     */
    private Long userId;

    /**
     * 当前登录用户角色
     */
    private String role;

    /**
     * 反馈结果：YES / NO / UNCERTAIN
     */
    private String feedbackResult;

    /**
     * 反馈补充说明
     */
    private String content;
}