package com.zhku.agriwarningplatform.module.feedback.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:35
 */
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的反馈分页列表项 VO
 */
@Data
public class FeedbackPageItemVO {

    /**
     * 反馈ID
     */
    private Long id;

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
     * 反馈时间
     */
    private LocalDateTime gmtCreate;
}
