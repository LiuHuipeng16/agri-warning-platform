package com.zhku.agriwarningplatform.module.feedback.service.dto;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:33
 */
import lombok.Data;

/**
 * 反馈目标AI普通问答摘要 DTO
 */
@Data
public class FeedbackAIChatTargetDetailDTO {

    private String chatId;

    private String question;

    private String answerSummary;
}
