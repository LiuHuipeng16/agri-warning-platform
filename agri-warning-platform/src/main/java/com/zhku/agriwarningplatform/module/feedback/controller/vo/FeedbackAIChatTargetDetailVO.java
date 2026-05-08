package com.zhku.agriwarningplatform.module.feedback.controller.vo;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:37
 */
import lombok.Data;

/**
 * 反馈目标AI普通问答摘要 VO
 */
@Data
public class FeedbackAIChatTargetDetailVO {

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * 用户提问
     */
    private String question;

    /**
     * AI回答摘要
     */
    private String answerSummary;
}
