package com.zhku.agriwarningplatform.module.feedback.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:35
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑反馈参数
 */
@Data
public class FeedbackUpdateParam {

    /**
     * 反馈ID
     */
    @NotNull(message = "反馈ID不能为空")
    private Long id;

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
