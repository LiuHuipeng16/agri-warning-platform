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
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.util.JacksonUtils;
import com.zhku.agriwarningplatform.module.ai.mapper.AIMapper;
import com.zhku.agriwarningplatform.module.ai.mapper.dataobject.*;
import com.zhku.agriwarningplatform.module.ai.service.AIService;
import com.zhku.agriwarningplatform.module.ai.service.dto.*;
import com.zhku.agriwarningplatform.module.ai.support.KnowledgeVectorDocumentBuilder;
import com.zhku.agriwarningplatform.module.crop.mapper.dataobject.CropDO;
import com.zhku.agriwarningplatform.module.pest.mapper.dataobject.PestDO;
import com.zhku.agriwarningplatform.module.warning.mapper.dataobject.WarningDO;
import com.zhku.agriwarningplatform.module.weather.service.WeatherService;
import com.zhku.agriwarningplatform.module.weather.service.dto.WeatherForecastDTO;
import com.zhku.agriwarningplatform.module.weather.service.dto.WeatherTodayDTO;
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
import org.springframework.data.redis.core.StringRedisTemplate;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    private static final String ROLE_ADMIN = "ADMIN";

    private static final String WARNING_EXPLANATION_CACHE_KEY_PREFIX = "ai:warning:explanation:";
    private static final String RISK_REPORT_CACHE_KEY_PREFIX = "ai:risk:report:days:";

    private static final long WARNING_EXPLANATION_CACHE_SECONDS = 3600L;
    private static final long RISK_REPORT_CACHE_SECONDS = 600L;

    private final StringRedisTemplate stringRedisTemplate;
    private final WeatherService weatherService;
    public AIServiceImpl(AIMapper aiMapper,
                         @Qualifier("deepSeekChatClient") ChatClient deepSeekChatClient,
                         @Qualifier("dashScopeChatClient") ChatClient dashScopeChatClient,
                         SimpleVectorStore simpleVectorStore,
                         KnowledgeVectorDocumentBuilder knowledgeVectorDocumentBuilder,
                         AliyunOSSOperator aliyunOSSOperator,
                         ObjectMapper objectMapper,
                         StringRedisTemplate stringRedisTemplate,
                         WeatherService weatherService) {
        this.aiMapper = aiMapper;
        this.deepSeekChatClient = deepSeekChatClient;
        this.dashScopeChatClient = dashScopeChatClient;
        this.simpleVectorStore = simpleVectorStore;
        this.knowledgeVectorDocumentBuilder = knowledgeVectorDocumentBuilder;
        this.aliyunOSSOperator = aliyunOSSOperator;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.weatherService = weatherService;
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
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        aiExecutor.execute(() -> {
            try {
                validateAssistantChatReq(reqDTO);
            } catch (ServiceException e) {
                sendServiceExceptionAsSseError(emitter, e);
                return;
            }

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
                                sendSseError(emitter, 500, "AI服务响应异常，请稍后重试");
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
                                    updateAssistantMessage(finalAssistantMessageId, reqDTO.getUserId(), displayedContent.toString(), status);
                                }
                            } catch (Exception e) {
                                log.error("保存悬浮AI回复异常, chatId={}", reqDTO.getChatId(), e);
                            } finally {
                                stopFlagMap.remove(stopKey);
                                safeComplete(emitter);
                            }
                        }
                );

                Long finalAssistantMessageId1 = assistantMessageId;
                emitter.onCompletion(() -> {
                    disposable.dispose();
                    stopFlagMap.remove(stopKey);
                });

                emitter.onTimeout(() -> {
                    disposable.dispose();
                    try {
                        if (finalAssistantMessageId1 != null && finished.compareAndSet(false, true)) {
                            updateAssistantMessage(finalAssistantMessageId1, reqDTO.getUserId(), displayedContent.toString(), MESSAGE_STATUS_FAILED);
                        }
                        sendSseError(emitter, 500, "AI响应超时，请稍后重试");
                    } catch (Exception e) {
                        log.error("悬浮AI超时处理失败, chatId={}", reqDTO.getChatId(), e);
                    } finally {
                        stopFlagMap.remove(stopKey);
                        safeComplete(emitter);
                    }
                });

            } catch (ServiceException e) {
                stopFlagMap.remove(stopKey);
                sendServiceExceptionAsSseError(emitter, e);
            } catch (Exception e) {
                log.error("悬浮AI对话异常, chatId={}", reqDTO.getChatId(), e);
                stopFlagMap.remove(stopKey);
                sendSseError(emitter, 500, "AI服务异常，请稍后重试");
                safeComplete(emitter);
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
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        aiExecutor.execute(() -> {
            try {
                validateChatStreamReq(reqDTO);
            } catch (ServiceException e) {
                sendServiceExceptionAsSseError(emitter, e);
                return;
            }

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
                                sendSseError(emitter, 500, "AI服务响应异常，请稍后重试");
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
                                    updateAssistantMessage(finalAssistantMessageId, reqDTO.getUserId(), displayedContent.toString(), status);
                                }
                            } catch (Exception e) {
                                log.error("保存AI回复异常, chatId={}", reqDTO.getChatId(), e);
                            } finally {
                                stopFlagMap.remove(stopKey);
                                safeComplete(emitter);
                            }
                        }
                );

                Long finalAssistantMessageId1 = assistantMessageId;
                emitter.onCompletion(() -> {
                    disposable.dispose();
                    stopFlagMap.remove(stopKey);
                });

                emitter.onTimeout(() -> {
                    disposable.dispose();
                    try {
                        if (finalAssistantMessageId1 != null && finished.compareAndSet(false, true)) {
                            updateAssistantMessage(finalAssistantMessageId1, reqDTO.getUserId(), displayedContent.toString(), MESSAGE_STATUS_FAILED);
                        }
                        sendSseError(emitter, 500, "AI响应超时，请稍后重试");
                    } catch (Exception e) {
                        log.error("AI超时处理失败, chatId={}", reqDTO.getChatId(), e);
                    } finally {
                        stopFlagMap.remove(stopKey);
                        safeComplete(emitter);
                    }
                });

            } catch (ServiceException e) {
                stopFlagMap.remove(stopKey);
                sendServiceExceptionAsSseError(emitter, e);
            } catch (Exception e) {
                log.error("独立AI对话异常, chatId={}", reqDTO.getChatId(), e);
                stopFlagMap.remove(stopKey);
                sendSseError(emitter, 500, "AI服务异常，请稍后重试");
                safeComplete(emitter);
            }
        });

        return emitter;
    }
    @Override
    public SseEmitter chatImageStream(AIChatImageStreamReqDTO reqDTO) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        aiExecutor.execute(() -> {
            try {
                validateChatImageStreamReq(reqDTO);
            } catch (ServiceException e) {
                sendServiceExceptionAsSseError(emitter, e);
                return;
            }

            String stopKey = buildStopKey(reqDTO.getUserId(), reqDTO.getChatId());
            stopFlagMap.put(stopKey, false);

            AtomicBoolean finished = new AtomicBoolean(false);

            emitter.onTimeout(() -> {
                log.warn("图文AI响应超时, chatId={}", reqDTO.getChatId());

                try {
                    sendSseError(emitter, 500, "AI响应超时，请稍后重试或换一张更清晰的图片");
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

            Long assistantMessageId = null;
            StringBuilder displayedContent = new StringBuilder();

            try {
                ensureChatSessionExists(reqDTO.getChatId(), reqDTO.getUserId(), reqDTO.getPrompt());

                List<String> imageUrlList = uploadImages(reqDTO.getImages());
                String imageUrlsJson = writeJson(imageUrlList);

                // 第一阶段：DashScope 只负责图片识别
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
                String environmentRiskContext = buildImageEnvironmentRiskContext();

                log.info("图文问答环境风险上下文={}", environmentRiskContext);

                String finalPrompt = buildImageChatFinalPrompt(
                        historyContext,
                        reqDTO.getPrompt(),
                        imageAnalysis,
                        environmentRiskContext
                );

                log.info("图文问答最终Prompt={}", finalPrompt);

                PromptTemplate promptTemplate = buildRagPromptTemplate();
                QuestionAnswerAdvisor advisor = buildQuestionAnswerAdvisor(promptTemplate, reqDTO.getPrompt());

                // 第二阶段：DeepSeek 负责农业知识 + 环境风险 + 结构化综合回答
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

                                sendSseError(emitter, 500, "AI服务响应异常，请稍后重试");

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
                                sendSseError(emitter, 500, "AI回复保存失败，请稍后重试");
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

                        sendSseError(emitter, 500, "AI响应超时，请稍后重试或换一张更清晰的图片");

                    } catch (Exception e) {
                        log.error("图文AI超时处理失败, chatId={}", reqDTO.getChatId(), e);
                    } finally {
                        stopFlagMap.remove(stopKey);
                        safeComplete(emitter);
                    }
                });

            } catch (ServiceException e) {
                log.error("图文AI业务异常, chatId={}", reqDTO.getChatId(), e);

                if (assistantMessageId != null && finished.compareAndSet(false, true)) {
                    try {
                        updateAssistantMessage(
                                assistantMessageId,
                                reqDTO.getUserId(),
                                displayedContent.toString(),
                                MESSAGE_STATUS_FAILED
                        );
                    } catch (Exception ex) {
                        log.error("业务异常场景更新图文AI消息失败, chatId={}", reqDTO.getChatId(), ex);
                    }
                }

                stopFlagMap.remove(stopKey);
                sendServiceExceptionAsSseError(emitter, e);

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
                    sendSseError(emitter, 500, "AI图文识别失败，请稍后重试");
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
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        aiExecutor.execute(() -> {
            try {
                if (reqDTO == null || reqDTO.getWarningId() == null || reqDTO.getWarningId() <= 0) {
                    throw new ServiceException(AIErrorCode.WARNING_ID_INVALID);
                }
            } catch (ServiceException e) {
                sendServiceExceptionAsSseError(emitter, e);
                return;
            }

            String stopKey = buildStopKey(reqDTO.getUserId(), "warning_suggestion_" + reqDTO.getWarningId());
            stopFlagMap.put(stopKey, false);

            try {
                AIWarningSuggestionContextDO contextDO = aiMapper.getWarningSuggestionContextByWarningId(reqDTO.getWarningId());
                if (contextDO == null) {
                    sendSseError(emitter, 404, "预警不存在");
                    safeComplete(emitter);
                    return;
                }

                String prompt = buildWarningSuggestionPrompt(contextDO);

                Flux<String> flux = deepSeekChatClient.prompt()
                        .user(prompt)
                        .stream()
                        .content();

                Disposable disposable = flux.subscribe(
                        chunk -> {
                            if (Boolean.TRUE.equals(stopFlagMap.get(stopKey))) {
                                return;
                            }
                            if (StringUtils.hasText(chunk)) {
                                sendSseChunk(emitter, chunk);
                            }
                        },
                        error -> {
                            log.error("预警AI建议生成异常, warningId={}", reqDTO.getWarningId(), error);
                            sendSseError(emitter, 500, "AI服务响应异常，请稍后重试");
                            stopFlagMap.remove(stopKey);
                            safeComplete(emitter);
                        },
                        () -> {
                            stopFlagMap.remove(stopKey);
                            safeComplete(emitter);
                        }
                );

                emitter.onCompletion(() -> {
                    disposable.dispose();
                    stopFlagMap.remove(stopKey);
                });

                emitter.onTimeout(() -> {
                    disposable.dispose();
                    sendSseError(emitter, 500, "AI响应超时，请稍后重试");
                    stopFlagMap.remove(stopKey);
                    safeComplete(emitter);
                });

            } catch (ServiceException e) {
                stopFlagMap.remove(stopKey);
                sendServiceExceptionAsSseError(emitter, e);
            } catch (Exception e) {
                log.error("生成预警AI建议异常, warningId={}", reqDTO.getWarningId(), e);
                stopFlagMap.remove(stopKey);
                sendSseError(emitter, 500, "AI服务异常，请稍后重试");
                safeComplete(emitter);
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

    @Override
    public SseEmitter generateWarningExplanationStream(AIWarningExplanationReqDTO reqDTO) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        aiExecutor.execute(() -> {
            try {
                validateWarningExplanationReq(reqDTO);
            } catch (ServiceException e) {
                sendServiceExceptionAsSseError(emitter, e);
                return;
            }

            String cacheKey = WARNING_EXPLANATION_CACHE_KEY_PREFIX + reqDTO.getWarningId();

            try {
                if (!Boolean.TRUE.equals(reqDTO.getRefresh())) {
                    String cachedText = stringRedisTemplate.opsForValue().get(cacheKey);
                    if (StringUtils.hasText(cachedText)) {
                        sendCachedTextAsStream(emitter, cachedText);
                        return;
                    }
                } else {
                    stringRedisTemplate.delete(cacheKey);
                }

                AIWarningExplanationContextDO contextDO =
                        aiMapper.getWarningExplanationContextByWarningId(reqDTO.getWarningId());

                if (contextDO == null) {
                    sendSseError(emitter, 404, "预警不存在");
                    safeComplete(emitter);
                    return;
                }

                String prompt = buildWarningExplanationPrompt(contextDO);
                StringBuilder answerBuilder = new StringBuilder();

                Flux<String> flux = deepSeekChatClient.prompt()
                        .user(prompt)
                        .stream()
                        .content();

                Disposable disposable = flux.subscribe(
                        chunk -> {
                            if (!StringUtils.hasText(chunk)) {
                                return;
                            }
                            answerBuilder.append(chunk);
                            sendSseChunk(emitter, chunk);
                        },
                        error -> {
                            log.error("AI预警解释生成异常, warningId={}", reqDTO.getWarningId(), error);
                            sendSseError(emitter, 500, "AI预警解释生成失败");
                            safeComplete(emitter);
                        },
                        () -> {
                            try {
                                String answer = answerBuilder.toString();
                                if (StringUtils.hasText(answer)) {
                                    stringRedisTemplate.opsForValue().set(
                                            cacheKey,
                                            answer,
                                            WARNING_EXPLANATION_CACHE_SECONDS,
                                            TimeUnit.SECONDS
                                    );
                                }
                            } catch (Exception e) {
                                log.error("AI预警解释缓存写入异常, warningId={}", reqDTO.getWarningId(), e);
                            } finally {
                                safeComplete(emitter);
                            }
                        }
                );

                emitter.onCompletion(disposable::dispose);

                emitter.onTimeout(() -> {
                    disposable.dispose();
                    sendSseError(emitter, 500, "AI响应超时，请稍后重试");
                    safeComplete(emitter);
                });

            } catch (ServiceException e) {
                sendServiceExceptionAsSseError(emitter, e);
            } catch (Exception e) {
                log.error("AI预警解释处理异常, warningId={}", reqDTO.getWarningId(), e);
                sendSseError(emitter, 500, "AI预警解释生成失败");
                safeComplete(emitter);
            }
        });

        return emitter;
    }
    @Override
    public SseEmitter generateRiskReportStream(AIRiskReportReqDTO reqDTO) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        aiExecutor.execute(() -> {
            try {
                validateRiskReportReq(reqDTO);
            } catch (ServiceException e) {
                sendServiceExceptionAsSseError(emitter, e);
                return;
            }

            String cacheKey = RISK_REPORT_CACHE_KEY_PREFIX + reqDTO.getDays();

            try {
                if (!Boolean.TRUE.equals(reqDTO.getRefresh())) {
                    String cachedText = stringRedisTemplate.opsForValue().get(cacheKey);
                    if (StringUtils.hasText(cachedText)) {
                        sendCachedTextAsStream(emitter, cachedText);
                        return;
                    }
                } else {
                    stringRedisTemplate.delete(cacheKey);
                }

                List<WeatherForecastDTO> forecastList = weatherService.getForecastWeather(reqDTO.getDays());

                LocalDate startDate = LocalDate.now().plusDays(1);
                LocalDate endDate = LocalDate.now().plusDays(reqDTO.getDays());

                List<AIRiskReportWarningContextDO> warningList =
                        aiMapper.getRiskReportWarningContextList(startDate, endDate);

                if (CollectionUtils.isEmpty(forecastList) && CollectionUtils.isEmpty(warningList)) {
                    sendSseError(emitter, 400, "暂无可分析的天气或预警数据");
                    safeComplete(emitter);
                    return;
                }

                String prompt = buildRiskReportPrompt(reqDTO.getDays(), forecastList, warningList);
                StringBuilder answerBuilder = new StringBuilder();

                Flux<String> flux = deepSeekChatClient.prompt()
                        .user(prompt)
                        .stream()
                        .content();

                Disposable disposable = flux.subscribe(
                        chunk -> {
                            if (!StringUtils.hasText(chunk)) {
                                return;
                            }
                            answerBuilder.append(chunk);
                            sendSseChunk(emitter, chunk);
                        },
                        error -> {
                            log.error("未来农业风险趋势分析生成异常, days={}", reqDTO.getDays(), error);
                            sendSseError(emitter, 500, "风险趋势分析生成失败");
                            safeComplete(emitter);
                        },
                        () -> {
                            try {
                                String answer = answerBuilder.toString();
                                if (StringUtils.hasText(answer)) {
                                    stringRedisTemplate.opsForValue().set(
                                            cacheKey,
                                            answer,
                                            RISK_REPORT_CACHE_SECONDS,
                                            TimeUnit.SECONDS
                                    );
                                }
                            } catch (Exception e) {
                                log.error("未来农业风险趋势分析缓存写入异常, days={}", reqDTO.getDays(), e);
                            } finally {
                                safeComplete(emitter);
                            }
                        }
                );

                emitter.onCompletion(disposable::dispose);

                emitter.onTimeout(() -> {
                    disposable.dispose();
                    sendSseError(emitter, 500, "AI响应超时，请稍后重试");
                    safeComplete(emitter);
                });

            } catch (ServiceException e) {
                sendServiceExceptionAsSseError(emitter, e);
            } catch (Exception e) {
                log.error("未来农业风险趋势分析处理异常, days={}", reqDTO.getDays(), e);
                sendSseError(emitter, 500, "风险趋势分析生成失败");
                safeComplete(emitter);
            }
        });

        return emitter;
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

    private void validateWarningExplanationReq(AIWarningExplanationReqDTO reqDTO) {

        if (reqDTO == null || !StringUtils.hasText(reqDTO.getWarningIdStr())) {
            throw new ServiceException(AIErrorCode.WARNING_ID_INVALID);
        }

        Long warningId;

        try {
            warningId = Long.valueOf(reqDTO.getWarningIdStr());
        } catch (Exception e) {
            throw new ServiceException(AIErrorCode.WARNING_ID_INVALID);
        }

        if (warningId <= 0) {
            throw new ServiceException(AIErrorCode.WARNING_ID_INVALID);
        }

        Boolean refresh = parseBooleanParam(reqDTO.getRefreshStr());

        reqDTO.setWarningId(warningId);
        reqDTO.setRefresh(refresh);
    }

    private void validateRiskReportReq(AIRiskReportReqDTO reqDTO) {

        if (reqDTO == null) {
            throw new ServiceException(AIErrorCode.RISK_REPORT_DAYS_INVALID);
        }

        Integer days = 7;

        if (StringUtils.hasText(reqDTO.getDaysStr())) {

            try {
                days = Integer.valueOf(reqDTO.getDaysStr());
            } catch (Exception e) {
                throw new ServiceException(AIErrorCode.RISK_REPORT_DAYS_INVALID);
            }
        }

        if (days < 1 || days > 7) {
            throw new ServiceException(AIErrorCode.RISK_REPORT_DAYS_INVALID);
        }

        Boolean refresh = parseBooleanParam(reqDTO.getRefreshStr());

        if (Boolean.TRUE.equals(refresh)
                && !ROLE_ADMIN.equals(reqDTO.getRole())) {
            throw new ServiceException(AIErrorCode.REFRESH_FORBIDDEN);
        }

        reqDTO.setDays(days);
        reqDTO.setRefresh(refresh);
    }
    private Boolean parseBooleanParam(String value) {

        if (!StringUtils.hasText(value)) {
            return false;
        }

        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw new ServiceException(AIErrorCode.REFRESH_PARAM_INVALID);
    }
    private void sendCachedTextAsStream(SseEmitter emitter, String cachedText) {
        try {
            List<String> chunks = splitCachedTextToChunks(cachedText);
            for (String chunk : chunks) {
                if (!StringUtils.hasText(chunk)) {
                    continue;
                }
                sendSseChunk(emitter, chunk);
                Thread.sleep(80L);
            }
            emitter.complete();
        } catch (Exception e) {
            log.error("缓存文本流式返回失败", e);
            completeWithError(emitter, e);
        }
    }

    private List<String> splitCachedTextToChunks(String text) {
        List<String> result = new ArrayList<>();

        if (!StringUtils.hasText(text)) {
            return result;
        }

        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = normalized.split("\n");

        for (String line : lines) {
            if (!StringUtils.hasText(line)) {
                continue;
            }

            String[] sentences = line.split("(?<=[。！？；])");
            for (String sentence : sentences) {
                if (StringUtils.hasText(sentence)) {
                    result.add(sentence.trim());
                }
            }
        }

        if (result.isEmpty()) {
            result.add(text);
        }

        return result;
    }
    private void sendSseError(SseEmitter emitter, Integer code, String msg) {
        try {
            CommonResult<Void> result = new CommonResult<>();
            result.setCode(code);
            result.setMsg(msg);
            result.setData(null);

            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(JacksonUtils.writeValueAsString(result)));
        } catch (Exception e) {
            log.error("发送SSE错误事件失败, code={}, msg={}", code, msg, e);
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


    private String buildWarningExplanationPrompt(AIWarningExplanationContextDO contextDO) {
        return """
            你是农业病虫害预警解释助手。
            请基于系统已经计算出的客观结果，生成一段适合普通用户阅读的预警解释。
            
            严格要求：
            1. 只能解释系统已有预警依据，不要否定系统规则结果。
            2. 不要编造未提供的数据。
            3. 要说明风险等级、风险评分、主要命中因素和防治提醒。
            4. 语气专业、简洁、自然。
            5. 不要说“根据上下文”“作为AI”等无关表述。
            
            预警基础信息：
            预警ID：%s
            预警标题：%s
            作物：%s
            病虫害：%s
            病虫害类型：%s
            风险等级：%s
            风险评分：%s
            预警类型：%s
            预警日期：%s
            
            风险评分明细JSON：
            %s
            
            规则命中依据JSON：
            %s
            
            命中规则：
            规则ID：%s
            规则名称：%s
            
            病虫害基础信息：
            症状：%s
            成因：%s
            防治措施：%s
            
            系统规则建议：
            %s
            
            请直接输出预警解释正文。
            """.formatted(
                contextDO.getWarningId(),
                getNullableString(contextDO.getWarningTitle()),
                getNullableString(contextDO.getCropName()),
                getNullableString(contextDO.getPestName()),
                getNullableString(contextDO.getPestType()),
                getNullableString(contextDO.getRiskLevel()),
                contextDO.getRiskScore() == null ? 0 : contextDO.getRiskScore(),
                getNullableString(contextDO.getWarningType()),
                contextDO.getWarningDate(),
                getNullableString(contextDO.getRiskScoreDetail()),
                getNullableString(contextDO.getMatchDetail()),
                contextDO.getRuleId(),
                getNullableString(contextDO.getRuleName()),
                getNullableString(contextDO.getSymptoms()),
                getNullableString(contextDO.getCause()),
                getNullableString(contextDO.getPrevention()),
                getNullableString(contextDO.getSuggestion())
        );
    }

    private String buildRiskReportPrompt(Integer days,
                                         List<WeatherForecastDTO> forecastList,
                                         List<AIRiskReportWarningContextDO> warningList) {
        return """
            你是农业病虫害风险趋势分析助手。
            请基于未来天气、未来预警、风险评分、作物和病虫害分布，生成未来农业风险趋势分析。
            
            严格要求：
            1. 这是“未来农业风险趋势分析”，不是日报、周报、本周总结。
            2. 只基于提供的数据分析，不要编造不存在的作物、病虫害、天气。
            3. 要说明整体风险趋势、重点风险病虫害、主要天气原因、巡查建议。
            4. 如果高风险较集中，要指出重点关注对象。
            5. 输出自然语言正文，不要输出JSON。
            6. 不要说“根据上下文”“作为AI”等无关表述。
            
            分析范围：
            未来 %s 天
            
            未来天气数据：
            %s
            
            未来预警数据：
            %s
            
            请直接输出分析正文。
            """.formatted(
                days,
                buildForecastWeatherText(forecastList),
                buildRiskWarningText(warningList)
        );
    }

    private String buildForecastWeatherText(List<WeatherForecastDTO> forecastList) {
        if (CollectionUtils.isEmpty(forecastList)) {
            return "暂无未来天气数据";
        }

        StringBuilder sb = new StringBuilder();

        for (WeatherForecastDTO dto : forecastList) {
            sb.append("日期：").append(getNullableString(dto.getDate())).append("，")
                    .append("城市：").append(getNullableString(dto.getCity())).append("，")
                    .append("最低温：").append(dto.getTempMin()).append("℃，")
                    .append("最高温：").append(dto.getTempMax()).append("℃，")
                    .append("平均湿度：").append(dto.getAvgHumidity()).append("%，")
                    .append("降水量：").append(dto.getPrecipitation()).append("mm，")
                    .append("最大风速：").append(dto.getMaxWindSpeed()).append("m/s，")
                    .append("天气：").append(getNullableString(dto.getWeatherDesc()))
                    .append("\n");
        }

        return sb.toString();
    }

    private String buildRiskWarningText(List<AIRiskReportWarningContextDO> warningList) {
        if (CollectionUtils.isEmpty(warningList)) {
            return "暂无未来预警数据";
        }

        StringBuilder sb = new StringBuilder();

        for (AIRiskReportWarningContextDO warning : warningList) {
            sb.append("日期：").append(warning.getWarningDate()).append("，")
                    .append("预警标题：").append(getNullableString(warning.getWarningTitle())).append("，")
                    .append("作物：").append(getNullableString(warning.getCropName())).append("，")
                    .append("病虫害：").append(getNullableString(warning.getPestName())).append("，")
                    .append("类型：").append(getNullableString(warning.getPestType())).append("，")
                    .append("风险等级：").append(getNullableString(warning.getRiskLevel())).append("，")
                    .append("风险评分：").append(warning.getRiskScore() == null ? 0 : warning.getRiskScore()).append("，")
                    .append("命中规则：").append(getNullableString(warning.getRuleName()))
                    .append("\n");
        }

        return sb.toString();
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
    private void sendServiceExceptionAsSseError(SseEmitter emitter, ServiceException e) {
        try {
            // 如果你的 ServiceException 不是 getErrorCode()，这里按你项目实际 getter 改一下
            Integer code = e.getCode();
            String msg = e.getMessage();

            sendSseError(emitter, code, msg);
        } catch (Exception ex) {
            log.error("解析ServiceException失败", ex);
            sendSseError(emitter, 500, "AI服务异常，请稍后重试");
        } finally {
            safeComplete(emitter);
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

    private String buildImageChatFinalPrompt(String historyContext,
                                             String userPrompt,
                                             String imageAnalysis,
                                             String environmentRiskContext) {
        return """
            %s

            用户当前问题：
            %s

            图片识别中间结果：
            %s

            当前农业环境风险上下文：
            %s

            你是农业病虫害图文综合分析助手。
            现在你需要基于“图片识别中间结果 + 用户问题 + 当前天气 + 未来天气 + 当前及未来预警风险 + 病虫害环境条件 + 知识库检索内容”生成最终回答。

            重要强制要求：
            1. 最终回答必须包含以下五个标题，标题名称不能改：
               【诊断结果】
               【风险等级】
               【判断依据】
               【防治建议】
               【后续观察】

            2. 【判断依据】中必须明确分成以下四项，名称不能省略：
               1. 图片依据：
               2. 环境依据：
               3. 预警依据：
               4. 知识库依据：

            3. 如果“当前农业环境风险上下文”中存在当前天气、未来天气、湿度、降水、风速、预警、风险评分、适宜温湿度等信息，
               最终回答必须明确引用这些信息，不能只分析图片。

            4. 如果“当前农业环境风险上下文”中没有天气或预警数据，
               也必须在【判断依据】中明确写：
               环境依据：暂无天气数据 / 暂无未来天气数据
               预警依据：暂无当前或未来预警风险数据

            5. 不允许编造未提供的数据。
               如果没有具体数值，就用“当前环境数据不足”“暂无相关预警”表达。

            输出格式必须严格如下：

            【诊断结果】
            用1~2句话说明疑似作物、疑似病虫害或无法确定的原因。

            【风险等级】
            结合图片症状、当前天气、未来天气、预警风险和病虫害适宜环境条件，判断为：低风险 / 中风险 / 高风险 / 暂无法判断。
            如果图片症状或环境依据不足，必须写“暂无法判断”，不要硬判。

            【判断依据】
            1. 图片依据：
            说明图片中可见的症状，例如白色粉末、病斑、虫体、叶片颜色、受害部位等。
            2. 环境依据：
            必须说明当前天气或未来天气对病虫害发生的影响，例如湿度、降水、温度、风速。
            如果没有天气数据，明确写“暂无天气数据”。
            3. 预警依据：
            必须说明当前或未来是否存在相关预警、风险等级、风险评分、命中规则。
            如果没有预警数据，明确写“暂无当前或未来预警风险数据”。
            4. 知识库依据：
            结合病虫害常见症状、发生条件或防治知识进行说明。
            如果知识库依据不足，明确写“暂无足够知识库依据”。

            【防治建议】
            1. 优先给出农业管理建议，例如通风、排水、巡查、清除病叶、降低湿度、减少田间郁闭等。
            2. 如需用药，只能说“必要时咨询当地农技人员后规范用药”，不要编造具体药剂和剂量。
            3. 建议要结合环境风险，例如湿度高则强调通风降湿，未来降水多则强调排水和雨后巡查。

            【后续观察】
            说明用户接下来1~3天应该重点观察什么，例如病斑是否扩大、白色粉状物是否增加、叶片背面是否出现虫体、雨后是否扩散等。

            回答要求：
            1. 不要输出JSON。
            2. 不要说“根据上下文”“作为AI”。
            3. 不要把图片识别结果原样复制一遍，要整理成最终分析。
            4. 不要编造没有提供的天气、预警、病虫害名称和具体数值。
            5. 如果图片症状与环境风险不一致，要明确说明“图片症状与当前环境风险不完全一致，需要继续观察”。
            6. 语言要专业、简洁、适合农业系统页面展示。
            """.formatted(
                getNullableString(historyContext),
                getNullableString(userPrompt),
                getNullableString(imageAnalysis),
                getNullableString(environmentRiskContext)
        );
    }
    private String buildImageEnvironmentRiskContext() {
        StringBuilder sb = new StringBuilder();

        try {
            WeatherTodayDTO todayWeather = weatherService.getTodayWeather();
            sb.append("【当前天气】\n");
            sb.append("城市：").append(getNullableString(todayWeather.getCity())).append("\n");
            sb.append("日期：").append(getNullableString(todayWeather.getDate())).append("\n");
            sb.append("当前温度：").append(todayWeather.getTemperature()).append("℃\n");
            sb.append("最低温：").append(todayWeather.getTempMin()).append("℃\n");
            sb.append("最高温：").append(todayWeather.getTempMax()).append("℃\n");
            sb.append("湿度：").append(todayWeather.getHumidity()).append("%\n");
            sb.append("降水量：").append(todayWeather.getPrecipitation()).append("mm\n");
            sb.append("风速：").append(todayWeather.getWindSpeed()).append("m/s\n");
            sb.append("天气描述：").append(getNullableString(todayWeather.getWeatherDesc())).append("\n\n");
        } catch (Exception e) {
            log.warn("图文问答获取当前天气失败，已降级为空天气上下文", e);
            sb.append("【当前天气】暂无当前天气数据\n\n");
        }

        try {
            List<WeatherForecastDTO> forecastList = weatherService.getForecastWeather(3);
            sb.append("【未来三天天气】\n");

            if (CollectionUtils.isEmpty(forecastList)) {
                sb.append("暂无未来天气数据\n");
            } else {
                for (WeatherForecastDTO dto : forecastList) {
                    sb.append("日期：").append(getNullableString(dto.getDate()))
                            .append("，最低温：").append(dto.getTempMin()).append("℃")
                            .append("，最高温：").append(dto.getTempMax()).append("℃")
                            .append("，平均湿度：").append(dto.getAvgHumidity()).append("%")
                            .append("，降水量：").append(dto.getPrecipitation()).append("mm")
                            .append("，最大风速：").append(dto.getMaxWindSpeed()).append("m/s")
                            .append("，天气：").append(getNullableString(dto.getWeatherDesc()))
                            .append("\n");
                }
            }

            sb.append("\n");
        } catch (Exception e) {
            log.warn("图文问答获取未来天气失败，已降级为空天气上下文", e);
            sb.append("【未来三天天气】暂无未来天气数据\n\n");
        }

        try {
            List<AIImageEnvironmentRiskContextDO> riskContextList =
                    aiMapper.getImageEnvironmentRiskContextList();

            sb.append("【当前及未来环境风险】\n");

            if (CollectionUtils.isEmpty(riskContextList)) {
                sb.append("暂无当前或未来预警风险数据\n");
            } else {
                for (AIImageEnvironmentRiskContextDO context : riskContextList) {
                    sb.append("预警标题：").append(getNullableString(context.getWarningTitle()))
                            .append("，作物：").append(getNullableString(context.getCropName()))
                            .append("，病虫害：").append(getNullableString(context.getPestName()))
                            .append("，类型：").append(getNullableString(context.getPestType()))
                            .append("，风险等级：").append(getNullableString(context.getRiskLevel()))
                            .append("，风险评分：").append(context.getRiskScore() == null ? 0 : context.getRiskScore())
                            .append("，预警类型：").append(getNullableString(context.getWarningType()))
                            .append("，预警日期：").append(context.getWarningDate())
                            .append("，命中规则：").append(getNullableString(context.getRuleName()))
                            .append("，适宜温度：").append(getNullableString(context.getTemperatureRange()))
                            .append("，适宜湿度：").append(getNullableString(context.getHumidityRange()))
                            .append("，环境描述：").append(getNullableString(context.getEnvironmentDescription()))
                            .append("，系统建议：").append(getNullableString(context.getSuggestion()))
                            .append("\n");
                }
            }

        } catch (Exception e) {
            log.warn("图文问答获取环境风险上下文失败，已降级为空风险上下文", e);
            sb.append("【当前及未来环境风险】暂无当前或未来预警风险数据\n");
        }

        return sb.toString();
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