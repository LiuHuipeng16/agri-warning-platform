package com.zhku.agriwarningplatform.module.feedback.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:31
 */
import lombok.Data;

import java.time.LocalDate;

/**
 * 反馈分页查询 DTO
 */
@Data
public class FeedbackPageQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 当前登录用户ID
     */
    private Long userId;

    /**
     * 当前登录用户角色
     */
    private String role;

    /**
     * 用户名关键词，后台分页使用
     */
    private String username;

    /**
     * 反馈目标类型：WARNING / AI_IMAGE / AI_CHAT
     */
    private String targetType;

    /**
     * 反馈结果：YES / NO / UNCERTAIN
     */
    private String feedbackResult;

    /**
     * 作物ID，仅 WARNING 类型有效
     */
    private Long cropId;

    /**
     * 病虫害ID，仅 WARNING 类型有效
     */
    private Long pestId;

    /**
     * 反馈开始日期
     */
    private LocalDate dateStart;

    /**
     * 反馈结束日期
     */
    private LocalDate dateEnd;
}
