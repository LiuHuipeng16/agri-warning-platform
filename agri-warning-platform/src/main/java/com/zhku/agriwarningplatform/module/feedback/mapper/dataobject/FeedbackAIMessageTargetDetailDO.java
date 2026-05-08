package com.zhku.agriwarningplatform.module.feedback.mapper.dataobject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:30
 */
import lombok.Data;

/**
 * 反馈目标AI消息摘要 DO
 */
@Data
public class FeedbackAIMessageTargetDetailDO {

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * 用户提问
     */
    private String question;

    /**
     * 图片识别结果，仅 AI_IMAGE 使用
     */
    private String imageAnalysis;

    /**
     * AI回答摘要
     */
    private String answerSummary;
}