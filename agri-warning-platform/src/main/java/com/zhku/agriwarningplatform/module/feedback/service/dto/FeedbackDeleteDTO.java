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
 * 删除反馈 DTO
 */
@Data
public class FeedbackDeleteDTO {

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
}