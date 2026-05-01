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
import com.zhku.agriwarningplatform.module.ai.support.KnowledgeVectorDocumentBuilder;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhku.agriwarningplatform.common.util.AliyunOSSOperator;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    private static final Long SSE_TIMEOUT = 300000L;
    private static final Integer HISTORY_LIMIT = 8;

    private static final String ROLE_USER = "user";
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String MESSAGE_TYPE_TEXT = "TEXT";
    private static final String MESSAGE_TYPE_IMAGE_TEXT = "IMAGE_TEXT";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024L;

    private final AliyunOSSOperator aliyunOSSOperator;
    private final ObjectMapper objectMapper;
    private static final String MESSAGE_STATUS_STREAMING = "STREAMING";
    private static final String MESSAGE_STATUS_COMPLETED = "COMPLETED";
    private static final String MESSAGE_STATUS_STOPPED = "STOPPED";
    private static final String MESSAGE_STATUS_FAILED = "FAILED";

    private final AIMapper aiMapper;
    private final ChatClient deepSeekChatClient;
    private final ChatClient dashScopeChatClient;
    private final SimpleVectorStore simpleVectorStore;
    private final KnowledgeVectorDocumentBuilder knowledgeVectorDocumentBuilder;
    public AIServiceImpl(AIMapper aiMapper,
                         @Qualifier("deepSeekChatClient") ChatClient deepSeekChatClient,
                         @Qualifier("dashScopeChatClient") ChatClient dashScopeChatClient,
                         SimpleVectorStore simpleVectorStore,
                         KnowledgeVectorDocumentBuilder knowledgeVectorDocumentBuilder,
                         AliyunOSSOperator aliyunOSSOperator,
                         ObjectMapper objectMapper) {
        this.aiMapper = aiMapper;
        this.deepSeekChatClient = deepSeekChatClient;
        this.dashScopeChatClient = dashScopeChatClient;
        this.simpleVectorStore = simpleVectorStore;
        this.knowledgeVectorDocumentBuilder = knowledgeVectorDocumentBuilder;
        this.aliyunOSSOperator = aliyunOSSOperator;
        this.objectMapper = objectMapper;
    }
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

                Flux<String> flux = deepSeekChatClient.prompt()
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

                Flux<String> flux = deepSeekChatClient.prompt()
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
    public SseEmitter chatImageStream(AIChatImageStreamReqDTO reqDTO) {
        validateChatImageStreamReq(reqDTO);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        String stopKey = buildStopKey(reqDTO.getUserId(), reqDTO.getChatId());
        stopFlagMap.put(stopKey, false);

        AtomicBoolean finished = new AtomicBoolean(false);

        emitter.onTimeout(() -> {
            log.warn("图文AI响应超时, chatId={}", reqDTO.getChatId());

            try {
                sendSseError(emitter, "AI响应超时，请稍后重试或换一张更清晰的图片");
            } finally {
                stopFlagMap.remove(stopKey);
                finished.set(true);
                safeComplete(emitter);
            }
        });

        emitter.onError(error -> {
            log.error("图文AI SSE连接异常, chatId={}", reqDTO.getChatId(), error);
            stopFlagMap.remove(stopKey);
            finished.set(true);
        });

        emitter.onCompletion(() -> {
            log.info("图文AI SSE连接结束, chatId={}", reqDTO.getChatId());
            stopFlagMap.remove(stopKey);
            finished.set(true);
        });

        aiExecutor.execute(() -> {
            Long assistantMessageId = null;
            StringBuilder displayedContent = new StringBuilder();

            try {
                ensureChatSessionExists(reqDTO.getChatId(), reqDTO.getUserId(), reqDTO.getPrompt());

                List<String> imageUrlList = uploadImages(reqDTO.getImages());
                String imageUrlsJson = writeJson(imageUrlList);

                String imageAnalysis = analyzeImages(reqDTO.getPrompt(), imageUrlList, reqDTO.getImages());

                saveUserImageMessage(
                        reqDTO.getChatId(),
                        reqDTO.getUserId(),
                        reqDTO.getPrompt(),
                        imageUrlsJson,
                        imageAnalysis
                );

                assistantMessageId = createAssistantStreamingMessage(reqDTO.getChatId(), reqDTO.getUserId());

                List<AIChatMessageDO> recentMessages =
                        getRecentMessages(reqDTO.getChatId(), reqDTO.getUserId(), HISTORY_LIMIT);

                String historyContext = buildHistoryContext(recentMessages);
                String finalPrompt = buildImageChatFinalPrompt(historyContext, reqDTO.getPrompt(), imageAnalysis);

                PromptTemplate promptTemplate = buildRagPromptTemplate();
                QuestionAnswerAdvisor advisor = buildQuestionAnswerAdvisor(promptTemplate, reqDTO.getPrompt());

                Flux<String> flux = dashScopeChatClient.prompt()
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
                            log.error("图文AI流式输出异常, chatId={}", reqDTO.getChatId(), error);

                            try {
                                if (finalAssistantMessageId != null && finished.compareAndSet(false, true)) {
                                    String status = Boolean.TRUE.equals(stopFlagMap.get(stopKey))
                                            ? MESSAGE_STATUS_STOPPED
                                            : MESSAGE_STATUS_FAILED;

                                    updateAssistantMessage(
                                            finalAssistantMessageId,
                                            reqDTO.getUserId(),
                                            displayedContent.toString(),
                                            status
                                    );
                                }

                                sendSseError(emitter, "AI服务响应异常，请稍后重试");

                            } catch (Exception ex) {
                                log.error("处理图文AI异常失败, chatId={}", reqDTO.getChatId(), ex);
                            } finally {
                                stopFlagMap.remove(stopKey);
                                safeComplete(emitter);
                            }
                        },
                        () -> {
                            try {
                                if (finalAssistantMessageId != null && finished.compareAndSet(false, true)) {
                                    String status = Boolean.TRUE.equals(stopFlagMap.get(stopKey))
                                            ? MESSAGE_STATUS_STOPPED
                                            : MESSAGE_STATUS_COMPLETED;

                                    updateAssistantMessage(
                                            finalAssistantMessageId,
                                            reqDTO.getUserId(),
                                            displayedContent.toString(),
                                            status
                                    );
                                }

                                safeComplete(emitter);

                            } catch (Exception e) {
                                log.error("保存图文AI回复异常, chatId={}", reqDTO.getChatId(), e);
                                sendSseError(emitter, "AI回复保存失败，请稍后重试");
                                safeComplete(emitter);
                            } finally {
                                stopFlagMap.remove(stopKey);
                            }
                        }
                );

                emitter.onCompletion(() -> {
                    disposable.dispose();
                    stopFlagMap.remove(stopKey);
                    finished.set(true);
                });

                emitter.onTimeout(() -> {
                    disposable.dispose();

                    try {
                        if (finalAssistantMessageId != null && finished.compareAndSet(false, true)) {
                            updateAssistantMessage(
                                    finalAssistantMessageId,
                                    reqDTO.getUserId(),
                                    displayedContent.toString(),
                                    MESSAGE_STATUS_FAILED
                            );
                        }

                        sendSseError(emitter, "AI响应超时，请稍后重试或换一张更清晰的图片");

                    } catch (Exception e) {
                        log.error("图文AI超时处理失败, chatId={}", reqDTO.getChatId(), e);
                    } finally {
                        stopFlagMap.remove(stopKey);
                        safeComplete(emitter);
                    }
                });

            } catch (Exception e) {
                log.error("独立AI图文对话异常, chatId={}", reqDTO.getChatId(), e);

                if (assistantMessageId != null && finished.compareAndSet(false, true)) {
                    try {
                        updateAssistantMessage(
                                assistantMessageId,
                                reqDTO.getUserId(),
                                displayedContent.toString(),
                                MESSAGE_STATUS_FAILED
                        );
                    } catch (Exception ex) {
                        log.error("异常场景更新图文AI消息失败, chatId={}", reqDTO.getChatId(), ex);
                    }
                }

                try {
                    sendSseError(emitter, "AI图文识别失败，请稍后重试");
                } finally {
                    stopFlagMap.remove(stopKey);
                    safeComplete(emitter);
                }
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
            log.info("查询到消息数量: {}", messageDOList.size());
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

                Flux<String> flux = deepSeekChatClient.prompt()
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
                documents.add(knowledgeVectorDocumentBuilder.buildKnowledgeDocument(qaDO));
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
    private void validateChatImageStreamReq(AIChatImageStreamReqDTO reqDTO) {
        if (!StringUtils.hasText(reqDTO.getChatId())) {
            throw new ServiceException(AIErrorCode.CHAT_ID_EMPTY);
        }
        if (!StringUtils.hasText(reqDTO.getPrompt())) {
            throw new ServiceException(AIErrorCode.PROMPT_EMPTY);
        }
        if (reqDTO.getImages() == null || reqDTO.getImages().length == 0) {
            throw new ServiceException(AIErrorCode.IMAGE_EMPTY);
        }
        if (reqDTO.getImages().length > 3) {
            throw new ServiceException(AIErrorCode.IMAGE_COUNT_INVALID);
        }

        for (MultipartFile image : reqDTO.getImages()) {
            if (image == null || image.isEmpty()) {
                throw new ServiceException(AIErrorCode.IMAGE_EMPTY);
            }
            if (image.getSize() > MAX_IMAGE_SIZE) {
                throw new ServiceException(AIErrorCode.IMAGE_SIZE_TOO_LARGE);
            }
            if (!isAllowedImageType(image.getContentType())) {
                throw new ServiceException(AIErrorCode.IMAGE_TYPE_NOT_SUPPORT);
            }
        }
    }
    private boolean isAllowedImageType(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/jpg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
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

    private Long saveMessage(String chatId,
                             Long userId,
                             String role,
                             String content,
                             String messageType,
                             String imageUrls,
                             String imageAnalysis,
                             String messageStatus) {
        AIChatMessageDO messageDO = new AIChatMessageDO();
        messageDO.setChatId(chatId);
        messageDO.setUserId(userId);
        messageDO.setRole(role);
        messageDO.setContent(content);
        messageDO.setMessageType(messageType);
        messageDO.setImageUrls(imageUrls);
        messageDO.setImageAnalysis(imageAnalysis);
        messageDO.setMessageStatus(messageStatus);
        messageDO.setDeleteFlag(0);

        int rows = aiMapper.insertChatMessage(messageDO);
        if (rows != 1) {
            throw new ServiceException(AIErrorCode.CHAT_MESSAGE_SAVE_FAILED);
        }
        return messageDO.getId();
    }
    private Long saveUserMessage(String chatId, Long userId, String prompt) {
        return saveMessage(
                chatId,
                userId,
                ROLE_USER,
                prompt,
                MESSAGE_TYPE_TEXT,
                null,
                null,
                MESSAGE_STATUS_COMPLETED
        );
    }

    private Long saveUserImageMessage(String chatId,
                                      Long userId,
                                      String prompt,
                                      String imageUrls,
                                      String imageAnalysis) {
        return saveMessage(
                chatId,
                userId,
                ROLE_USER,
                prompt,
                MESSAGE_TYPE_IMAGE_TEXT,
                imageUrls,
                imageAnalysis,
                MESSAGE_STATUS_COMPLETED
        );
    }
    private Long createAssistantStreamingMessage(String chatId, Long userId) {
        return saveMessage(
                chatId,
                userId,
                ROLE_ASSISTANT,
                "",
                MESSAGE_TYPE_TEXT,
                null,
                null,
                MESSAGE_STATUS_STREAMING
        );
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

                if (MESSAGE_TYPE_IMAGE_TEXT.equals(messageDO.getMessageType())
                        && StringUtils.hasText(messageDO.getImageAnalysis())) {
                    sb.append("图片识别结果：")
                            .append(getNullableString(messageDO.getImageAnalysis()))
                            .append("\n");
                }
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
    private void sendSseError(SseEmitter emitter, String message) {
        try {

            String json = String.format(
                    "{\"code\":500,\"msg\":\"%s\",\"data\":null}",
                    message
            );

            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(json));

        } catch (Exception e) {
            log.warn("发送SSE错误事件失败, message={}", message, e);
        }
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.warn("关闭SSE连接失败", e);
        }
    }
    private void handleStreamError(SseEmitter emitter, String chatId, Throwable error) {
        log.error("AI流式输出异常, chatId={}", chatId, error);

        try {
            sendSseError(emitter, "AI响应异常，请稍后重试");
        } finally {
            safeComplete(emitter);
        }
    }

    private void completeWithError(SseEmitter emitter, Throwable error) {
        log.error("SSE异常结束", error);

        try {
            sendSseError(emitter, "AI服务异常，请稍后重试");
        } finally {
            safeComplete(emitter);
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
    private List<String> uploadImages(MultipartFile[] images) {
        List<String> imageUrlList = new ArrayList<>();

        for (MultipartFile image : images) {
            try {

                String contentType = image.getContentType();

                String suffix = ".jpg";
                if ("image/png".equals(contentType)) {
                    suffix = ".png";
                } else if ("image/webp".equals(contentType)) {
                    suffix = ".webp";
                } else if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)) {
                    suffix = ".jpg";
                }

                // 关键：生成安全文件名
                String safeFileName = java.util.UUID.randomUUID().toString() + suffix;

                String imageUrl = aliyunOSSOperator.upload(image.getBytes(), safeFileName);

                imageUrlList.add(imageUrl);

            } catch (Exception e) {
                log.error("上传AI图文对话图片失败, fileName={}", image == null ? null : image.getOriginalFilename(), e);
                throw new ServiceException(AIErrorCode.IMAGE_UPLOAD_FAILED);
            }
        }

        return imageUrlList;
    }
    private String analyzeImages(String userPrompt, List<String> imageUrlList, MultipartFile[] images) {
        try {
            List<Media> mediaList = new ArrayList<>();

            for (int i = 0; i < imageUrlList.size(); i++) {
                String imageUrl = imageUrlList.get(i);
                String contentType = images[i].getContentType();

                if (!StringUtils.hasText(contentType)) {
                    contentType = "image/jpeg";
                }

                mediaList.add(new Media(
                        MimeTypeUtils.parseMimeType(contentType),
                        new URI(imageUrl).toURL().toURI()
                ));
            }

            String analysisPrompt = """
        你是农业病虫害图像识别助手。

        你的任务：
        只提取图片中与农业病虫害诊断有关的客观信息，作为后续回答的中间识别结果。

        用户问题：
        %s

        请严格按以下格式输出：

        【图片识别结果】
        1. 可见作物或部位：
        2. 可见症状：
        3. 疑似病虫害：
        4. 判断依据：
        5. 置信度：
        6. 需要进一步确认的信息：

        输出要求：
        1. 只描述图片中能看到的内容。
        2. 可以使用“疑似”“可能”为判断保留不确定性。
        3. 如果无法判断作物或病虫害，请明确写“无法仅凭图片确定”。
        4. 不要输出防治建议。
        5. 不要推荐农药。
        6. 不要写危害说明。
        7. 不要写总结。
        8. 不要把结果写成面向用户的完整诊断报告。
        """.formatted(userPrompt);

            UserMessage message = UserMessage.builder()
                    .text(analysisPrompt)
                    .media(mediaList)
                    .build();

            ChatResponse response = dashScopeChatClient
                    .prompt(new Prompt(message))
                    .call()
                    .chatResponse();


            if (response == null
                    || response.getResult() == null
                    || response.getResult().getOutput() == null
                    || !StringUtils.hasText(response.getResult().getOutput().getText())) {
                throw new ServiceException(AIErrorCode.IMAGE_ANALYSIS_FAILED);
            }

            return response.getResult().getOutput().getText();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI图片识别异常", e);
            throw new ServiceException(AIErrorCode.IMAGE_ANALYSIS_FAILED);
        }
    }

    private String buildImageChatFinalPrompt(String historyContext, String userPrompt, String imageAnalysis) {
        return """
            %s

            用户当前问题：
            %s

            图片识别结果：
            %s

            请结合图片识别结果、用户问题以及知识库检索内容进行回答。

            回答要求：
            1. 如果图片症状明显，可以说明疑似病虫害，但不要过度绝对化。
            2. 如果无法仅凭图片确定，请明确说明可能存在多种情况。
            3. 防治建议要结合农业病虫害场景，尽量具体、自然、清晰。
            4. 不要编造具体天气、预警记录、规则命中结果。
            5. 不要出现“根据上下文”“根据检索结果”等生硬表述。
            """.formatted(
                getNullableString(historyContext),
                getNullableString(userPrompt),
                getNullableString(imageAnalysis)
        ).trim();
    }

    private String writeJson(List<String> imageUrlList) {
        try {
            return objectMapper.writeValueAsString(imageUrlList);
        } catch (Exception e) {
            log.error("图片URL列表序列化失败", e);
            throw new ServiceException(AIErrorCode.CHAT_MESSAGE_SAVE_FAILED);
        }
    }

    private List<String> readImageUrlList(String imageUrls) {
        if (!StringUtils.hasText(imageUrls)) {
            return null;
        }

        try {
            return objectMapper.readValue(imageUrls, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("图片URL列表反序列化失败, imageUrls={}", imageUrls, e);
            return null;
        }
    }
    // ==================== DTO转换 ====================

    private List<AIChatMessageDTO> convertToMessageDTOList(List<AIChatMessageDO> messageDOList) {
        List<AIChatMessageDTO> dtoList = new ArrayList<>();

        for (AIChatMessageDO messageDO : messageDOList) {
            AIChatMessageDTO dto = new AIChatMessageDTO();
            dto.setRole(messageDO.getRole());
            dto.setContent(messageDO.getContent());
            dto.setMessageType(StringUtils.hasText(messageDO.getMessageType())
                    ? messageDO.getMessageType()
                    : MESSAGE_TYPE_TEXT);

            if (MESSAGE_TYPE_IMAGE_TEXT.equals(messageDO.getMessageType())) {
                dto.setImageUrls(readImageUrlList(messageDO.getImageUrls()));
                dto.setImageAnalysis(messageDO.getImageAnalysis());
            } else {
                dto.setImageUrls(null);
                dto.setImageAnalysis(null);
            }

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