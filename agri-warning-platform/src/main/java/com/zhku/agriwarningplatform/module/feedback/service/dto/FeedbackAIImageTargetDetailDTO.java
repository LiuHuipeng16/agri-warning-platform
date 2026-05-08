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
 * 反馈目标AI图文问诊摘要 DTO
 */
@Data
public class FeedbackAIImageTargetDetailDTO {

    private String chatId;

    private String question;

    private String imageAnalysis;

    private String answerSummary;
}
