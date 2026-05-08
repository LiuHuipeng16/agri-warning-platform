package com.zhku.agriwarningplatform.module.feedback.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:34
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交反馈参数
 */
@Data
public class FeedbackSubmitParam {

    /**
     * 反馈目标类型：WARNING / AI_IMAGE / AI_CHAT
     */
    @NotBlank(message = "反馈目标类型不能为空")
    private String targetType;

    /**
     * 反馈目标ID
     */
    @NotBlank(message = "反馈目标ID不能为空")
    private String targetId;

    /**
     * 反馈结果：YES / NO / UNCERTAIN
     */
    @NotBlank(message = "反馈结果不能为空")
    private String feedbackResult;

    /**
     * 反馈补充说明
     */
    @Size(max = 500, message = "反馈补充说明不能超过500个字符")
    private String content;
}
