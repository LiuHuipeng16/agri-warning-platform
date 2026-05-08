package com.zhku.agriwarningplatform.module.feedback.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:29
 */
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 反馈分页列表项 DO
 */
@TableName("feedback")
@Data
public class FeedbackPageItemDO {

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
     * 反馈时间
     */
    private LocalDateTime gmtCreate;
}
