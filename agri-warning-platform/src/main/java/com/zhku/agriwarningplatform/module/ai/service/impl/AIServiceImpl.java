package com.zhku.agriwarningplatform.module.ai.service.impl;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-14
 * Time: 11:18
 */
import com.zhku.agriwarningplatform.common.errorcode.AIErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.module.ai.mapper.AIMapper;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.AIChatMessageDO;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.AIChatSessionDO;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.AIWarningSuggestionContextDO;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.LightweightKnowledgeBaseEnhancedQaDO;
import com.zhku.agriwarningplatform.module.ai.service.AIService;
import com.zhku.agriwarningplatform.module.ai.service.dto.*;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDO;
import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.PestDO;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private static final Long SSE_TIMEOUT = 300000L;
    private static final Integer HISTORY_LIMIT = 8;

    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";

    private static final String MESSAGE_STATUS_STREAMING = "STREAMING";
    private static final String MESSAGE_STATUS_COMPLETED = "COMPLETED";
    private static final String MESSAGE_STATUS_STOPPED = "STOPPED";
    private static final String MESSAGE_STATUS_FAILED = "FAILED";

    private final AIMapper aiMapper;
    private final ChatClient chatClient;
    private final SimpleVectorStore simpleVectorStore;

    /**
     * 单机开发 / 答辩演示场景下足够使用
     */
    private final ExecutorService aiExecutor = Executors.newCachedThreadPool();

    /**
     * 停止输出标记
     * key: userId + "_" + chatId
     */
    private final Map<String, Boolean> stopFlagMap = new ConcurrentHashMap<>();

    @Override
    public SseEmitter assistantChatStream(AIAssistantChatReqDTO reqDTO) {
        validateAssistantChatReq(reqDTO);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        aiExecutor.execute(() -> {
            String stopKey = buildStopKey(reqDTO.getUserId(), reqDTO.getChatId());
            stopFlagMap.put(stopKey, false);

            Long assistantMessageId = null;
            StringBuilder displayedContent = new StringBuilder();
            AtomicBoolean finished = new AtomicBoolean(false);

            try {
                saveUserMessage(reqDTO.getChatId(), reqDTO.getUserId(), reqDTO.getPrompt());
                assistantMessageId = createAssistantStreamingMessage(reqDTO.getChatId(), reqDTO.getUserId());

                List<AIChatMessageDO> recentMessages = getRecentMessages(reqDTO.getChatId(), reqDTO.getUserId(), HISTORY_LIMIT);
                String historyContext = buildHistoryContext(recentMessages);
                String pageContext = buildPageContext(reqDTO.getContextType(), reqDTO.getContextId());
                String finalPrompt = buildAssistantFinalPrompt(historyContext, pageContext, reqDTO.getPrompt());

                PromptTemplate promptTemplate = buildRagPromptTemplate();
                QuestionAnswerAdvisor advisor = buildQuestionAnswerAdvisor(promptTemplate, reqDTO.getPrompt());

                Flux<String> flux = chatClient.prompt()
                        .user(finalPrompt)
                        .advisors(advisor)
                        .stream()
                        .content();

                Long finalAssistantMessageId = assistantMessageId;

                Disposable disposable = flux.subscribe(
                        chunk -> {
                            if (Boolean.TRUE.equals(stopFlagMap.get(stopKey))) {
                                return;
                            }
                            if (!StringUtils.hasText(chunk)) {
                                return;
                            }

                            sendSseChunk(emitter, chunk);
                            displayedContent.append(chunk);
                        },
                        error -> {
                            try {
                                if (finalAssistantMessageId != null && finished.compareAndSet(false, true)) {
                                    String status = Boolean.TRUE.equals(stopFlagMap.get(stopKey))
                                            ? MESSAGE_STATUS_STOPPED
                                            : MESSAGE_STATUS_FAILED;
                                    updateAssistantMessage(finalAssistantMessageId, reqDTO.getUserId(), displayedContent.toString(), status);
                                }
                            } catch (Exception ex) {
                                log.error("更新悬浮AI消息失败, chatId={}", reqDTO.getChatId(), ex);
                            } finally {
                                handleStreamError(emitter, reqDTO.getChatId(), error);
                                stopFlagMap.remove(stopKey);
                            }
                        },
                        () -> {
                            try {
                                if (finalAssistantMessageId != null && finished.compareAndSet(false, true)) {
                                    String status = Boolean.TRUE.equals(stopFlagMap.get(stopKey))
                                            ? MESSAGE_STATUS_STOPPED
                                            : MESSAGE_STATUS_COMPLETED;
                                    updateAssistantMessage(finalAssistantMessageId, reqDTO.getUserId(), displayedContent.toString(), status);
                                }
                                emitter.complete();
                            } catch (Exception e) {
                                log.error("保存悬浮AI回复异常, chatId={}", reqDTO.getChatId(), e);
                                completeWithError(emitter, e);
                            } finally {
                                stopFlagMap.remove(stopKey);
                            }
                        }
                );

                Long finalAssistantMessageId1 = assistantMessageId;
                emitter.onCompletion(() -> {
                    try {
                        disposable.dispose();
                        if (finalAssistantMessageId1 != null && finished.compareAndSet(false, true)) {
                            String status = Boolean.TRUE.equals(stopFlagMap.get(stopKey))
                                    ? MESSAGE_STATUS_STOPPED
                                    : MESSAGE_STATUS_FAILED;
                            updateAssistantMessage(finalAssistantMessageId1, reqDTO.getUserId(), displayedContent.toString(), status);
                        }
                    } catch (Exception e) {
                        log.error("SSE连接断开后兜底更新悬浮AI消息失败, chatId={}", reqDTO.getChatId(), e);
                    } finally {
                        stopFlagMap.remove(stopKey);
                    }
                });

                emitter.onTimeout(() -> {
                    try {
                        disposable.dispose();
                        if (finalAssistantMessageId1 != null && finished.compareAndSet(false, true)) {
                            updateAssistantMessage(finalAssistantMessageId1, reqDTO.getUserId(), displayedContent.toString(), MESSAGE_STATUS_FAILED);
                        }
                    } catch (Exception e) {
                        log.error("悬浮AI SSE超时后更新消息失败, chatId={}", reqDTO.getChatId(), e);
                    } finally {
                        stopFlagMap.remove(stopKey);
                        try {
                            emitter.complete();
                        } catch (Exception ex) {
                            log.error("悬浮AI SSE超时后关闭连接失败, chatId={}", reqDTO.getChatId(), ex);
                        }
                    }
                });

            } catch (Exception e) {
                log.error("悬浮AI对话异常, chatId={}", reqDTO.getChatId(), e);

                if (assistantMessageId != null && finished.compareAndSet(false, true)) {
                    try {
                        updateAssistantMessage(assistantMessageId, reqDTO.getUserId(), displayedContent.toString(), MESSAGE_STATUS_FAILED);
                    } catch (Exception ex) {
                        log.error("异常场景更新悬浮AI消息失败, chatId={}", reqDTO.getChatId(), ex);
                    }
                }

                stopFlagMap.remove(stopKey);
                completeWithError(emitter, e);
            }
        });

        return emitter;
    }

    @Override
    public List<AIChatMessageDTO> getAssistantHistory(AIChatHistoryQueryDTO queryDTO) {
        validateChatHistoryQuery(queryDTO);

        try {
            List<AIChatMessageDO> messageDOList = aiMapper.getChatMessageHistoryByChatId(queryDTO.getChatId(), queryDTO.getUserId());
            if (CollectionUtils.isEmpty(messageDOList)) {
                return new ArrayList<>();
            }
            return convertToMessageDTOList(messageDOList);
        } catch (Exception e) {
            log.error("查询悬浮AI会话历史异常, chatId={}", queryDTO.getChatId(), e);
            throw new ServiceException(AIErrorCode.CHAT_MESSAGE_QUERY_FAILED);
        }
    }

    @Override
    public SseEmitter chatStream(AIChatStreamReqDTO reqDTO) {
        validateChatStreamReq(reqDTO);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        aiExecutor.execute(() -> {
            String stopKey = buildStopKey(reqDTO.getUserId(), reqDTO.getChatId());
            stopFlagMap.put(stopKey, false);

            Long assistantMessageId = null;
            StringBuilder displayedContent = new StringBuilder();
            AtomicBoolean finished = new AtomicBoolean(false);

            try {
                ensureChatSessionExists(reqDTO.getChatId(), reqDTO.getUserId(), reqDTO.getPrompt());

                saveUserMessage(reqDTO.getChatId(), reqDTO.getUserId(), reqDTO.getPrompt());
                assistantMessageId = createAssistantStreamingMessage(reqDTO.getChatId(), reqDTO.getUserId());

                List<AIChatMessageDO> recentMessages = getRecentMessages(reqDTO.getChatId(), reqDTO.getUserId(), HISTORY_LIMIT);
                String historyContext = buildHistoryContext(recentMessages);
                String finalPrompt = buildChatFinalPrompt(historyContext, reqDTO.getPrompt());

                PromptTemplate promptTemplate = buildRagPromptTemplate();
                QuestionAnswerAdvisor advisor = buildQuestionAnswerAdvisor(promptTemplate, reqDTO.getPrompt());

                Flux<String> flux = chatClient.prompt()
                        .user(finalPrompt)
                        .advisors(advisor)
                        .stream()
                        .content();

                Long finalAssistantMessageId = assistantMessageId;

                Disposable disposable = flux.subscribe(
                        chunk -> {
                            if (Boolean.TRUE.equals(stopFlagMap.get(stopKey))) {
                                return;
                            }
                            if (!StringUtils.hasText(chunk)) {
                                return;
                            }

                            sendSseChunk(emitter, chunk);
                            displayedContent.append(chunk);
                        },
                        error -> {
                            try {
                                if (finalAssistantMessageId != null && finished.compareAndSet(false, true)) {
                                    String status = Boolean.TRUE.equals(stopFlagMap.get(stopKey))
                                            ? MESSAGE_STATUS_STOPPED
                                            : MESSAGE_STATUS_FAILED;
                                    updateAssistantMessage(finalAssistantMessageId, reqDTO.getUserId(), displayedContent.toString(), status);
                                }
                            } catch (Exception ex) {
                                log.error("更新AI消息失败, chatId={}", reqDTO.getChatId(), ex);
                            } finally {
                                handleStreamError(emitter, reqDTO.getChatId(), error);
                                stopFlagMap.remove(stopKey);
                            }
                        },
                        () -> {
                            try {
                                if (finalAssistantMessageId != null && finished.compareAndSet(false, true)) {
                                    String status = Boolean.TRUE.equals(stopFlagMap.get(stopKey))
                                            ? MESSAGE_STATUS_STOPPED
                                            : MESSAGE_STATUS_COMPLETED;
                                    updateAssistantMessage(finalAssistantMessageId, reqDTO.getUserId(), displayedContent.toString(), status);
                                }
                                emitter.complete();
                            } catch (Exception e) {
                                log.error("保存AI回复异常, chatId={}", reqDTO.getChatId(), e);
                                completeWithError(emitter, e);
                            } finally {
                                stopFlagMap.remove(stopKey);
                            }
                        }
                );

                Long finalAssistantMessageId1 = assistantMessageId;
                emitter.onCompletion(() -> {
                    try {
                        disposable.dispose();
                        if (finalAssistantMessageId1 != null && finished.compareAndSet(false, true)) {
                            String status = Boolean.TRUE.equals(stopFlagMap.get(stopKey))
                                    ? MESSAGE_STATUS_STOPPED
                                    : MESSAGE_STATUS_FAILED;
                            updateAssistantMessage(finalAssistantMessageId1, reqDTO.getUserId(), displayedContent.toString(), status);
                        }
                    } catch (Exception e) {
                        log.error("SSE连接断开后兜底更新AI消息失败, chatId={}", reqDTO.getChatId(), e);
                    } finally {
                        stopFlagMap.remove(stopKey);
                    }
                });

                emitter.onTimeout(() -> {
                    try {
                        disposable.dispose();
                        if (finalAssistantMessageId1 != null && finished.compareAndSet(false, true)) {
                            updateAssistantMessage(finalAssistantMessageId1, reqDTO.getUserId(), displayedContent.toString(), MESSAGE_STATUS_FAILED);
                        }
                    } catch (Exception e) {
                        log.error("SSE超时后更新AI消息失败, chatId={}", reqDTO.getChatId(), e);
                    } finally {
                        stopFlagMap.remove(stopKey);
                        try {
                            emitter.complete();
                        } catch (Exception ex) {
                            log.error("SSE超时后关闭连接失败, chatId={}", reqDTO.getChatId(), ex);
                        }
                    }
                });

            } catch (Exception e) {
                log.error("独立AI对话异常, chatId={}", reqDTO.getChatId(), e);

                if (assistantMessageId != null && finished.compareAndSet(false, true)) {
                    try {
                        updateAssistantMessage(assistantMessageId, reqDTO.getUserId(), displayedContent.toString(), MESSAGE_STATUS_FAILED);
                    } catch (Exception ex) {
                        log.error("异常场景更新AI消息失败, chatId={}", reqDTO.getChatId(), ex);
                    }
                }

                stopFlagMap.remove(stopKey);
                completeWithError(emitter, e);
            }
        });

        return emitter;
    }
    @Override
    public List<AIChatSessionItemDTO> getChatSessionList(Long userId) {
        try {
            List<AIChatSessionDO> sessionDOList = aiMapper.getChatSessionListByUserId(userId);
            if (CollectionUtils.isEmpty(sessionDOList)) {
                return new ArrayList<>();
            }
            return convertToSessionItemDTOList(sessionDOList);
        } catch (Exception e) {
            log.error("查询AI会话列表异常, userId={}", userId, e);
            throw new ServiceException(AIErrorCode.CHAT_MESSAGE_QUERY_FAILED);
        }
    }

    @Override
    public List<AIChatMessageDTO> getChatHistory(AIChatHistoryQueryDTO queryDTO) {
        validateChatHistoryQuery(queryDTO);
        checkChatSessionOwnership(queryDTO.getChatId(), queryDTO.getUserId());

        try {
            List<AIChatMessageDO> messageDOList = aiMapper.getChatMessageHistoryByChatId(queryDTO.getChatId(), queryDTO.getUserId());
            if (CollectionUtils.isEmpty(messageDOList)) {
                return new ArrayList<>();
            }
            return convertToMessageDTOList(messageDOList);
        } catch (Exception e) {
            log.error("查询AI会话历史异常, chatId={}", queryDTO.getChatId(), e);
            throw new ServiceException(AIErrorCode.CHAT_MESSAGE_QUERY_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createChatSession(AIChatCreateReqDTO reqDTO) {
        validateChatCreateReq(reqDTO);

        try {
            AIChatSessionDO existSession = aiMapper.getSessionByChatIdAndUserId(reqDTO.getChatId(), reqDTO.getUserId());
            if (existSession != null) {
                return true;
            }

            AIChatSessionDO sessionDO = new AIChatSessionDO();
            sessionDO.setChatId(reqDTO.getChatId());
            sessionDO.setUserId(reqDTO.getUserId());
            sessionDO.setSessionType("CHAT");
            sessionDO.setContextType("NONE");
            sessionDO.setContextId(null);
            sessionDO.setTitle(buildDefaultTitle(reqDTO.getTitle(), null));
            sessionDO.setDeleteFlag(0);

            int rows = aiMapper.insertChatSession(sessionDO);
            if (rows != 1) {
                throw new ServiceException(AIErrorCode.CHAT_SESSION_CREATE_FAILED);
            }

            return true;
        } catch (DuplicateKeyException e) {
            log.warn("AI会话已存在, chatId={}", reqDTO.getChatId(), e);
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("新增AI会话异常, chatId={}", reqDTO.getChatId(), e);
            throw new ServiceException(AIErrorCode.CHAT_SESSION_CREATE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateChatTitle(AIChatUpdateTitleReqDTO reqDTO) {
        validateUpdateTitleReq(reqDTO);
        checkChatSessionOwnership(reqDTO.getChatId(), reqDTO.getUserId());

        try {
            int rows = aiMapper.updateChatSessionTitleByChatId(reqDTO.getChatId(), reqDTO.getUserId(), reqDTO.getTitle());
            if (rows != 1) {
                throw new ServiceException(AIErrorCode.CHAT_SESSION_UPDATE_FAILED);
            }
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改AI会话标题异常, chatId={}", reqDTO.getChatId(), e);
            throw new ServiceException(AIErrorCode.CHAT_SESSION_UPDATE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteChatSession(AIChatHistoryQueryDTO queryDTO) {
        validateChatHistoryQuery(queryDTO);
        checkChatSessionOwnership(queryDTO.getChatId(), queryDTO.getUserId());

        try {
            int sessionRows = aiMapper.deleteChatSessionByChatId(queryDTO.getChatId(), queryDTO.getUserId());
            aiMapper.deleteChatMessagesByChatId(queryDTO.getChatId(), queryDTO.getUserId());

            if (sessionRows != 1) {
                throw new ServiceException(AIErrorCode.CHAT_SESSION_DELETE_FAILED);
            }

            stopFlagMap.remove(buildStopKey(queryDTO.getUserId(), queryDTO.getChatId()));
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除AI会话异常, chatId={}", queryDTO.getChatId(), e);
            throw new ServiceException(AIErrorCode.CHAT_SESSION_DELETE_FAILED);
        }
    }

    @Override
    public boolean stopChat(AIChatStopReqDTO reqDTO) {
        if (!StringUtils.hasText(reqDTO.getChatId())) {
            throw new ServiceException(AIErrorCode.CHAT_ID_EMPTY);
        }

        String stopKey = buildStopKey(reqDTO.getUserId(), reqDTO.getChatId());
        stopFlagMap.put(stopKey, true);

        try {
            AIChatMessageDO lastAssistantMessage = aiMapper.getLastAssistantMessageByChatId(reqDTO.getChatId(), reqDTO.getUserId());
            if (lastAssistantMessage != null && MESSAGE_STATUS_STREAMING.equals(lastAssistantMessage.getMessageStatus())) {
                aiMapper.updateChatMessageContentAndStatusById(
                        lastAssistantMessage.getId(),
                        reqDTO.getUserId(),
                        lastAssistantMessage.getContent(),
                        MESSAGE_STATUS_STOPPED
                );
            }
            return true;
        } catch (Exception e) {
            log.error("停止AI输出异常, chatId={}", reqDTO.getChatId(), e);
            throw new ServiceException(AIErrorCode.CHAT_STREAM_STOP_FAILED);
        }
    }

    @Override
    public SseEmitter generateWarningSuggestionStream(AIWarningSuggestionReqDTO reqDTO) {
        if (reqDTO.getWarningId() == null || reqDTO.getWarningId() <= 0) {
            throw new ServiceException(AIErrorCode.WARNING_ID_INVALID);
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        aiExecutor.execute(() -> {
            String stopKey = buildStopKey(reqDTO.getUserId(), "warning_suggestion_" + reqDTO.getWarningId());
            stopFlagMap.put(stopKey, false);

            try {
                AIWarningSuggestionContextDO contextDO = aiMapper.getWarningSuggestionContextByWarningId(reqDTO.getWarningId());
                if (contextDO == null) {
                    throw new ServiceException(AIErrorCode.WARNING_NOT_EXIST);
                }

                String prompt = buildWarningSuggestionPrompt(contextDO);

                Flux<String> flux = chatClient.prompt()
                        .user(prompt)
                        .stream()
                        .content();

                StringBuilder answerBuilder = new StringBuilder();

                Disposable disposable = flux.subscribe(
                        chunk -> {
                            if (Boolean.TRUE.equals(stopFlagMap.get(stopKey))) {
                                return;
                            }
                            if (chunk != null) {
                                answerBuilder.append(chunk);
                                sendSseChunk(emitter, chunk);
                            }
                        },
                        error -> {
                            handleStreamError(emitter, "warning_suggestion_" + reqDTO.getWarningId(), error);
                            stopFlagMap.remove(stopKey);
                        },
                        () -> {
                            try {
                                emitter.complete();
                            } finally {
                                stopFlagMap.remove(stopKey);
                            }
                        }
                );

                emitter.onCompletion(() -> {
                    disposable.dispose();
                    stopFlagMap.remove(stopKey);
                });

                emitter.onTimeout(() -> {
                    disposable.dispose();
                    stopFlagMap.remove(stopKey);
                });

            } catch (Exception e) {
                log.error("生成预警AI建议异常, warningId={}", reqDTO.getWarningId(), e);
                stopFlagMap.remove(stopKey);
                completeWithError(emitter, e);
            }
        });

        return emitter;
    }

    @Override
    public void initKnowledgeBaseToVectorStore() {
        try {
            List<LightweightKnowledgeBaseEnhancedQaDO> qaDOList = aiMapper.getAllValidKnowledgeQaList();
            if (CollectionUtils.isEmpty(qaDOList)) {
                log.info("知识库为空，跳过向量库初始化");
                return;
            }

            List<Document> documents = new ArrayList<>();
            for (LightweightKnowledgeBaseEnhancedQaDO qaDO : qaDOList) {
                String cropName = getNullableString(aiMapper.getCropNameById(qaDO.getCropId()));
                String pestName = getNullableString(aiMapper.getPestNameById(qaDO.getPestId()));
                String symptoms = getNullableString(aiMapper.getPestSymptomsById(qaDO.getPestId()));

                String content = """
                        作物：%s
                        病虫害：%s
                        问题：%s
                        症状：%s
                        答案：%s
                        """.formatted(
                        cropName,
                        pestName,
                        getNullableString(qaDO.getQuestion()),
                        symptoms,
                        getNullableString(qaDO.getAnswer())
                );

                Document document = new Document(content, Map.of(
                        "id", String.valueOf(qaDO.getId()),
                        "cropId", String.valueOf(qaDO.getCropId() == null ? 0L : qaDO.getCropId()),
                        "pestId", String.valueOf(qaDO.getPestId() == null ? 0L : qaDO.getPestId())
                ));
                documents.add(document);
            }

            simpleVectorStore.add(documents);
            log.info("知识库初始化完成, 共加载{}条文档到向量库", documents.size());
        } catch (Exception e) {
            log.error("初始化知识库到向量库异常", e);
            throw new ServiceException(AIErrorCode.KNOWLEDGE_LOAD_FAILED);
        }
    }

    // ==================== 校验方法 ====================

    private void validateAssistantChatReq(AIAssistantChatReqDTO reqDTO) {
        if (!StringUtils.hasText(reqDTO.getChatId())) {
            throw new ServiceException(AIErrorCode.CHAT_ID_EMPTY);
        }
        if (!StringUtils.hasText(reqDTO.getPrompt())) {
            throw new ServiceException(AIErrorCode.PROMPT_EMPTY);
        }
        validateContextTypeAndId(reqDTO.getContextType(), reqDTO.getContextId());
    }

    private void validateChatStreamReq(AIChatStreamReqDTO reqDTO) {
        if (!StringUtils.hasText(reqDTO.getChatId())) {
            throw new ServiceException(AIErrorCode.CHAT_ID_EMPTY);
        }
        if (!StringUtils.hasText(reqDTO.getPrompt())) {
            throw new ServiceException(AIErrorCode.PROMPT_EMPTY);
        }
    }

    private void validateChatHistoryQuery(AIChatHistoryQueryDTO queryDTO) {
        if (!StringUtils.hasText(queryDTO.getChatId())) {
            throw new ServiceException(AIErrorCode.CHAT_ID_EMPTY);
        }
    }

    private void validateChatCreateReq(AIChatCreateReqDTO reqDTO) {
        if (!StringUtils.hasText(reqDTO.getChatId())) {
            throw new ServiceException(AIErrorCode.CHAT_ID_EMPTY);
        }
    }

    private void validateUpdateTitleReq(AIChatUpdateTitleReqDTO reqDTO) {
        if (!StringUtils.hasText(reqDTO.getChatId())) {
            throw new ServiceException(AIErrorCode.CHAT_ID_EMPTY);
        }
        if (!StringUtils.hasText(reqDTO.getTitle())) {
            throw new ServiceException(AIErrorCode.TITLE_EMPTY);
        }
    }

    private void validateContextTypeAndId(String contextType, Long contextId) {
        if (!StringUtils.hasText(contextType)) {
            return;
        }
        if (!List.of("CROP", "PEST", "WARNING", "NONE").contains(contextType)) {
            throw new ServiceException(AIErrorCode.CONTEXT_TYPE_INVALID);
        }
        if (!"NONE".equals(contextType) && (contextId == null || contextId <= 0)) {
            throw new ServiceException(AIErrorCode.CONTEXT_ID_INVALID);
        }
    }

    // ==================== 核心业务方法 ====================

    private void ensureChatSessionExists(String chatId, Long userId, String prompt) {
        AIChatSessionDO sessionDO = aiMapper.getSessionByChatIdAndUserId(chatId, userId);
        if (sessionDO != null) {
            return;
        }

        AIChatSessionDO newSession = new AIChatSessionDO();
        newSession.setChatId(chatId);
        newSession.setUserId(userId);
        newSession.setSessionType("CHAT");
        newSession.setContextType("NONE");
        newSession.setContextId(null);
        newSession.setTitle(buildDefaultTitle(null, prompt));
        newSession.setDeleteFlag(0);

        try {
            int rows = aiMapper.insertChatSession(newSession);
            if (rows != 1) {
                throw new ServiceException(AIErrorCode.CHAT_SESSION_CREATE_FAILED);
            }
        } catch (DuplicateKeyException e) {
            log.warn("AI会话已存在, chatId={}", chatId);
        }
    }

    private void checkChatSessionOwnership(String chatId, Long userId) {
        AIChatSessionDO sessionDO = aiMapper.getSessionByChatIdAndUserId(chatId, userId);
        if (sessionDO == null) {
            throw new ServiceException(AIErrorCode.CHAT_SESSION_NOT_EXIST);
        }
    }

    private Long saveMessage(String chatId, Long userId, String role, String content, String messageStatus) {
        AIChatMessageDO messageDO = new AIChatMessageDO();
        messageDO.setChatId(chatId);
        messageDO.setUserId(userId);
        messageDO.setRole(role);
        messageDO.setContent(content);
        messageDO.setMessageStatus(messageStatus);
        messageDO.setDeleteFlag(0);

        int rows = aiMapper.insertChatMessage(messageDO);
        if (rows != 1) {
            throw new ServiceException(AIErrorCode.CHAT_MESSAGE_SAVE_FAILED);
        }
        return messageDO.getId();
    }

    private Long saveUserMessage(String chatId, Long userId, String prompt) {
        return saveMessage(chatId, userId, ROLE_USER, prompt, MESSAGE_STATUS_COMPLETED);
    }

    private Long createAssistantStreamingMessage(String chatId, Long userId) {
        return saveMessage(chatId, userId, ROLE_ASSISTANT, "", MESSAGE_STATUS_STREAMING);
    }

    private void updateAssistantMessage(Long messageId, Long userId, String content, String status) {
        int rows = aiMapper.updateChatMessageContentAndStatusById(messageId, userId, content, status);
        if (rows != 1) {
            throw new ServiceException(AIErrorCode.CHAT_MESSAGE_SAVE_FAILED);
        }
    }

    private List<AIChatMessageDO> getRecentMessages(String chatId, Long userId, Integer limit) {
        List<AIChatMessageDO> messageDOList = aiMapper.getRecentChatMessagesByChatId(chatId, userId, limit);
        if (CollectionUtils.isEmpty(messageDOList)) {
            return new ArrayList<>();
        }
        messageDOList.sort(Comparator.comparing(AIChatMessageDO::getGmtCreate).thenComparing(AIChatMessageDO::getId));
        return messageDOList;
    }

    private String buildHistoryContext(List<AIChatMessageDO> messageDOList) {
        if (CollectionUtils.isEmpty(messageDOList)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("历史对话：\n");
        for (AIChatMessageDO messageDO : messageDOList) {
            if (ROLE_USER.equals(messageDO.getRole())) {
                sb.append("用户：").append(getNullableString(messageDO.getContent())).append("\n");
            } else if (ROLE_ASSISTANT.equals(messageDO.getRole())) {
                sb.append("助手：").append(getNullableString(messageDO.getContent())).append("\n");
            }
        }
        return sb.toString();
    }

    private String buildPageContext(String contextType, Long contextId) {
        if (!StringUtils.hasText(contextType) || "NONE".equals(contextType)) {
            return "";
        }

        try {
            switch (contextType) {
                case "CROP":
                    CropDO cropDO = aiMapper.getCropContextById(contextId);
                    if (cropDO == null) {
                        log.warn("AI页面上下文对应的作物不存在，已降级为无上下文模式，contextType={}, contextId={}",
                                contextType, contextId);
                        return "";
                    }
                    return """
                        当前页面信息：
                        该页面为作物详情页面。

                        当前作物信息：
                        作物名称：%s
                        作物分类：%s
                        作物简介：%s
                        作物描述：%s
                        """.formatted(
                            getNullableString(cropDO.getName()),
                            getNullableString(cropDO.getCategory()),
                            getNullableString(cropDO.getIntro()),
                            getNullableString(cropDO.getDescription())
                    );

                case "PEST":
                    PestDO pestDO = aiMapper.getPestContextById(contextId);
                    if (pestDO == null) {
                        log.warn("AI页面上下文对应的病虫害不存在，已降级为无上下文模式，contextType={}, contextId={}",
                                contextType, contextId);
                        return "";
                    }
                    return """
                        当前页面信息：
                        该页面为病虫害详情页面。

                        当前病虫害信息：
                        病虫害名称：%s
                        类型：%s
                        症状：%s
                        描述：%s
                        防治措施：%s
                        """.formatted(
                            getNullableString(pestDO.getName()),
                            getNullableString(pestDO.getType()),
                            getNullableString(pestDO.getSymptoms()),
                            getNullableString(pestDO.getDescription()),
                            getNullableString(pestDO.getPrevention())
                    );

                case "WARNING":
                    WarningDO warningDO = aiMapper.getWarningContextById(contextId);
                    if (warningDO == null) {
                        log.warn("AI页面上下文对应的预警不存在，已降级为无上下文模式，contextType={}, contextId={}",
                                contextType, contextId);
                        return "";
                    }

                    String cropName = getNullableString(aiMapper.getCropNameById(warningDO.getCropId()));
                    String pestName = getNullableString(aiMapper.getPestNameById(warningDO.getPestId()));

                    return """
                        当前页面信息：
                        该页面为农作物病虫害预警详情页面。

                        预警信息：
                        作物：%s
                        病虫害：%s
                        风险等级：%s
                        预警日期：%s
                        """.formatted(
                            cropName,
                            pestName,
                            getNullableString(warningDO.getRiskLevel()),
                            warningDO.getWarningDate() == null ? "" : warningDO.getWarningDate().toString()
                    );

                default:
                    return "";
            }
        } catch (Exception e) {
            log.warn("构建AI页面上下文异常，已降级为无上下文模式，contextType={}, contextId={}",
                    contextType, contextId, e);
            return "";
        }
    }

    private String buildAssistantFinalPrompt(String historyContext, String pageContext, String userPrompt) {
        return """
                %s
                %s

                用户问题：
                %s

                补充：
                如果用户问题涉及当前页面对象，请优先结合当前页面信息回答。
                """.formatted(
                getNullableString(historyContext),
                getNullableString(pageContext),
                getNullableString(userPrompt)
        ).trim();
    }

    private String buildChatFinalPrompt(String historyContext, String userPrompt) {
        return """
                %s

                用户当前问题：
                %s
                """.formatted(
                getNullableString(historyContext),
                getNullableString(userPrompt)
        ).trim();
    }

    private String buildWarningSuggestionPrompt(AIWarningSuggestionContextDO contextDO) {
        return """
                你是农业病虫害预警系统中的智能分析助手，请基于以下预警信息生成一段自然、清晰、简洁的智能建议。

                预警信息：
                预警标题：%s
                作物名称：%s
                病虫害名称：%s
                病虫害类型：%s
                症状：%s
                风险等级：%s
                预警类型：%s
                预警日期：%s
                命中规则：%s
                规则建议：%s

                请遵循以下要求：
                1. 回答要自然、清晰、简洁。
                2. 可以结合病虫害症状、风险等级、规则建议进行说明。
                3. 不要编造没有给出的具体天气数据、规则阈值或命中细节。
                4. 如果规则建议为空，可以结合病虫害常见防治思路给出合理建议。
                5. 不要出现“根据上下文”“根据系统提供的信息”等表述。
                """.formatted(
                getNullableString(contextDO.getWarningTitle()),
                getNullableString(contextDO.getCropName()),
                getNullableString(contextDO.getPestName()),
                getNullableString(contextDO.getPestType()),
                getNullableString(contextDO.getSymptoms()),
                getNullableString(contextDO.getRiskLevel()),
                getNullableString(contextDO.getWarningType()),
                contextDO.getWarningDate() == null ? "" : contextDO.getWarningDate().toString(),
                getNullableString(contextDO.getRuleName()),
                getNullableString(contextDO.getSuggestion())
        );
    }

    private PromptTemplate buildRagPromptTemplate() {
        String template = """
                你是农业病虫害预警系统中的智能问答助手，负责回答与作物病虫害、预警信息、防治建议相关的问题。

                用户问题：
                {query}

                下面是系统检索到的相关知识内容：
                ---------------------
                {question_answer_context}
                ---------------------

                请根据用户问题回答，并遵循以下要求：
                1. 如果检索到的知识内容与问题相关，应优先结合这些内容作答。
                2. 如果检索到的知识内容不足以完整回答问题，可以结合你的通用知识进行合理补充，但不要与已有知识内容冲突。
                3. 不要编造不存在的具体数据、明确的天气情况、预警记录、规则命中结果或知识库中没有依据的过于确定的专业结论。
                4. 如果问题涉及病虫害防治，可从症状、诱因、影响条件、防治建议等角度进行回答。
                5. 如果问题涉及预警信息，可结合风险等级、诱发因素、处理建议进行解释。
                6. 回答应尽量自然、清晰、简洁，避免出现“根据上下文”“根据检索结果”“根据您提供的信息”等表述。
                7. 如果当前问题缺少必要信息，或无法确定具体结论，可以明确说明存在多种可能，并建议结合实际情况进一步判断。
                8. 如果检索到的知识内容与模型已有知识冲突，以当前检索到的知识内容为优先参考。
                """;
        return PromptTemplate.builder().template(template).build();
    }

    private QuestionAnswerAdvisor buildQuestionAnswerAdvisor(PromptTemplate promptTemplate, String userPrompt) {
        return QuestionAnswerAdvisor.builder(simpleVectorStore)
                .promptTemplate(promptTemplate)
                .searchRequest(SearchRequest.builder()
                        .query(userPrompt)
                        .similarityThreshold(0.35)
                        .topK(4)
                        .build())
                .build();
    }

    private void sendSseChunk(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event().data(chunk));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleStreamError(SseEmitter emitter, String chatId, Throwable error) {
        log.error("AI流式输出异常, chatId={}", chatId, error);
        completeWithError(emitter, error);
    }

    private void completeWithError(SseEmitter emitter, Throwable error) {
        try {
            emitter.completeWithError(error);
        } catch (Exception e) {
            log.error("SSE异常结束失败", e);
        }
    }

    private String buildStopKey(Long userId, String chatId) {
        return userId + "_" + chatId;
    }

    private String buildDefaultTitle(String title, String prompt) {
        if (StringUtils.hasText(title)) {
            return title.trim();
        }
        if (!StringUtils.hasText(prompt)) {
            return "新对话";
        }
        String trimmed = prompt.trim();
        return trimmed.length() <= 20 ? trimmed : trimmed.substring(0, 20);
    }

    private String getNullableString(String value) {
        return value == null ? "" : value;
    }

    // ==================== DTO转换 ====================

    private List<AIChatMessageDTO> convertToMessageDTOList(List<AIChatMessageDO> messageDOList) {
        List<AIChatMessageDTO> dtoList = new ArrayList<>();
        for (AIChatMessageDO messageDO : messageDOList) {
            AIChatMessageDTO dto = new AIChatMessageDTO();
            dto.setRole(messageDO.getRole());
            dto.setContent(messageDO.getContent());
            dtoList.add(dto);
        }
        return dtoList;
    }

    private List<AIChatSessionItemDTO> convertToSessionItemDTOList(List<AIChatSessionDO> sessionDOList) {
        List<AIChatSessionItemDTO> dtoList = new ArrayList<>();
        for (AIChatSessionDO sessionDO : sessionDOList) {
            AIChatSessionItemDTO dto = new AIChatSessionItemDTO();
            dto.setChatId(sessionDO.getChatId());
            dto.setTitle(sessionDO.getTitle());
            dtoList.add(dto);
        }
        return dtoList;
    }
}