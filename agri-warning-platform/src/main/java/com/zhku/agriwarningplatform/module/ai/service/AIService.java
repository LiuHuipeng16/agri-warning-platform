package com.zhku.agriwarningplatform.module.ai.service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 11:17
 */
import com.zhku.agriwarningplatform.module.ai.service.dto.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AIService {

    /**
     * 悬浮AI对话（流式）
     */
    SseEmitter assistantChatStream(AIAssistantChatReqDTO reqDTO);

    /**
     * 获取悬浮AI当前会话历史
     */
    List<AIChatMessageDTO> getAssistantHistory(AIChatHistoryQueryDTO queryDTO);

    /**
     * 独立AI对话（流式）
     */
    SseEmitter chatStream(AIChatStreamReqDTO reqDTO);

    /**
     * 获取AI会话列表
     */
    List<AIChatSessionItemDTO> getChatSessionList(Long userId);

    /**
     * 获取AI会话历史
     */
    List<AIChatMessageDTO> getChatHistory(AIChatHistoryQueryDTO queryDTO);

    /**
     * 新增AI会话
     */
    boolean createChatSession(AIChatCreateReqDTO reqDTO);

    /**
     * 修改AI会话标题
     */
    boolean updateChatTitle(AIChatUpdateTitleReqDTO reqDTO);

    /**
     * 删除AI会话
     */
    boolean deleteChatSession(AIChatHistoryQueryDTO queryDTO);

    /**
     * 停止AI输出
     */
    boolean stopChat(AIChatStopReqDTO reqDTO);

    /**
     * 预警AI智能建议（流式）
     */
    SseEmitter generateWarningSuggestionStream(AIWarningSuggestionReqDTO reqDTO);

    /**
     * 初始化知识库到向量库
     */
    void initKnowledgeBaseToVectorStore();
}
