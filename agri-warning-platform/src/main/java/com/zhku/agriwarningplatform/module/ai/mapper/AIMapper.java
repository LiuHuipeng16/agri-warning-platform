package com.zhku.agriwarningplatform.module.ai.mapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 11:17
 */
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.AIChatMessageDO;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.AIChatSessionDO;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.AIWarningSuggestionContextDO;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.LightweightKnowledgeBaseEnhancedQaDO;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDO;
import com.zhku.agriwarningplatform.module.crop.vo.CropQueryRespVO;
import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.PestDO;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AIMapper {

    // ==================== 会话表（独立AI） ====================

    /**
     * 根据chatId查询会话
     */
    AIChatSessionDO getSessionByChatId(@Param("chatId") String chatId);

    /**
     * 根据chatId和userId查询会话
     */
    AIChatSessionDO getSessionByChatIdAndUserId(@Param("chatId") String chatId,
                                                @Param("userId") Long userId);

    /**
     * 查询独立AI会话列表
     */
    List<AIChatSessionDO> getChatSessionListByUserId(@Param("userId") Long userId);

    /**
     * 新增AI会话
     */
    int insertChatSession(AIChatSessionDO sessionDO);

    /**
     * 修改AI会话标题
     */
    int updateChatSessionTitleByChatId(@Param("chatId") String chatId,
                                       @Param("userId") Long userId,
                                       @Param("title") String title);

    /**
     * 逻辑删除AI会话
     */
    int deleteChatSessionByChatId(@Param("chatId") String chatId,
                                  @Param("userId") Long userId);

    // ==================== 消息表（悬浮助手 + 独立AI） ====================

    /**
     * 新增聊天消息
     */
    int insertChatMessage(AIChatMessageDO messageDO);

    /**
     * 查询当前chatId全部历史消息（按时间升序）
     */
    List<AIChatMessageDO> getChatMessageHistoryByChatId(@Param("chatId") String chatId,
                                                        @Param("userId") Long userId);

    /**
     * 查询当前chatId最近limit条消息（先倒序查，再由service转正序）
     */
    List<AIChatMessageDO> getRecentChatMessagesByChatId(@Param("chatId") String chatId,
                                                        @Param("userId") Long userId,
                                                        @Param("limit") Integer limit);

    /**
     * 逻辑删除chatId下的全部消息
     */
    int deleteChatMessagesByChatId(@Param("chatId") String chatId,
                                   @Param("userId") Long userId);

    // ==================== 知识库加载 ====================

    /**
     * 查询全部有效知识库问答
     */
    List<LightweightKnowledgeBaseEnhancedQaDO> getAllValidKnowledgeQaList();

    /**
     * 根据作物ID查询作物名称
     */
    String getCropNameById(@Param("cropId") Long cropId);

    /**
     * 根据病虫害ID查询病虫害名称
     */
    String getPestNameById(@Param("pestId") Long pestId);

    /**
     * 根据病虫害ID查询病虫害症状
     */
    String getPestSymptomsById(@Param("pestId") Long pestId);

    // ==================== 页面上下文查询 ====================

    /**
     * 查询作物上下文信息
     */
    CropDO getCropContextById(@Param("id") Long id);

    /**
     * 查询病虫害上下文信息
     */
    PestDO getPestContextById(@Param("id") Long id);

    /**
     * 查询预警上下文信息
     */
    WarningDO getWarningContextById(@Param("id") Long id);

    /**
     * 查询预警AI建议上下文
     */
    AIWarningSuggestionContextDO getWarningSuggestionContextByWarningId(@Param("warningId") Long warningId);

    /**
     * 根据消息ID更新消息内容和状态
     */
    int updateChatMessageContentAndStatusById(@Param("id") Long id,
                                              @Param("userId") Long userId,
                                              @Param("content") String content,
                                              @Param("messageStatus") String messageStatus);

    /**
     * 查询当前会话最后一条assistant消息
     */
    AIChatMessageDO getLastAssistantMessageByChatId(@Param("chatId") String chatId,
                                                    @Param("userId") Long userId);
}
