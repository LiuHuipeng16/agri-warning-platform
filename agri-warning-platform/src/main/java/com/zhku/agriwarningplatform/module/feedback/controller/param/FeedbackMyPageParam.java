package com.zhku.agriwarningplatform.module.feedback.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:34
 */
import com.zhku.agriwarningplatform.common.page.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 我的反馈分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FeedbackMyPageParam extends PageParam {

    /**
     * 反馈目标类型：WARNING / AI_IMAGE / AI_CHAT
     */
    private String targetType;

    /**
     * 反馈结果：YES / NO / UNCERTAIN
     */
    private String feedbackResult;
}
