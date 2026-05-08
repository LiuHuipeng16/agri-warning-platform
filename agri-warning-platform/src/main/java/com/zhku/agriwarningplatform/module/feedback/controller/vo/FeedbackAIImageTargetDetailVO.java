package com.zhku.agriwarningplatform.module.feedback.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:36
 */
import lombok.Data;

/**
 * 反馈目标AI图文问诊摘要 VO
 */
@Data
public class FeedbackAIImageTargetDetailVO {

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * 用户提问
     */
    private String question;

    /**
     * 图片识别结果
     */
    private String imageAnalysis;

    /**
     * AI回答摘要
     */
    private String answerSummary;
}
