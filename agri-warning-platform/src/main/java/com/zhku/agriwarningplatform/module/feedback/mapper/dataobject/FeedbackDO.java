package com.zhku.agriwarningplatform.module.feedback.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:26
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈表 DO
 */
@TableName("feedback")
@Data
public class FeedbackDO {

    /**
     * 反馈ID
     */
    private Long id;

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