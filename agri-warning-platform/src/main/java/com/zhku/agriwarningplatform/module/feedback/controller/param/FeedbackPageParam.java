package com.zhku.agriwarningplatform.module.feedback.controller.param;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:34
 */
import com.zhku.agriwarningplatform.common.page.PageParam;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 后台反馈分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FeedbackPageParam extends PageParam {

    /**
     * 用户名关键词
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
    @Min(value = 1, message = "作物ID必须大于等于1")
    private Long cropId;

    /**
     * 病虫害ID，仅 WARNING 类型有效
     */
    @Min(value = 1, message = "病虫害ID必须大于等于1")
    private Long pestId;

    /**
     * 反馈开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateStart;

    /**
     * 反馈结束日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateEnd;
}
