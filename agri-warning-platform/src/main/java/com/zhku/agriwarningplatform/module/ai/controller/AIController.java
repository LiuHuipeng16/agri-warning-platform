package com.zhku.agriwarningplatform.module.ai.controller;

import com.zhku.agriwarningplatform.common.errorcode.AIErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.util.JacksonUtils;
import com.zhku.agriwarningplatform.common.util.JwtUtils;
import com.zhku.agriwarningplatform.module.ai.controller.param.AIAssistantChatStreamParam;
import com.zhku.agriwarningplatform.module.ai.controller.param.AIAssistantHistoryParam;
import com.zhku.agriwarningplatform.module.ai.controller.param.AIChatCreateParam;
import com.zhku.agriwarningplatform.module.ai.controller.param.AIChatStopParam;
import com.zhku.agriwarningplatform.module.ai.controller.param.AIChatStreamParam;
import com.zhku.agriwarningplatform.module.ai.controller.param.AIChatUpdateTitleParam;
import com.zhku.agriwarningplatform.module.ai.controller.vo.AIChatMessageVO;
import com.zhku.agriwarningplatform.module.ai.controller.vo.AIChatSessionItemVO;
import com.zhku.agriwarningplatform.module.ai.service.AIService;
import com.zhku.agriwarningplatform.module.ai.service.dto.AIAssistantChatReqDTO;
import com.zhku.agriwarningplatform.module.ai.service.dto.AIChatCreateReqDTO;
import com.zhku.agriwarningplatform.module.ai.service.dto.AIChatHistoryQueryDTO;
import com.zhku.agriwarningplatform.module.ai.service.dto.AIChatMessageDTO;
import com.zhku.agriwarningplatform.module.ai.service.dto.AIChatSessionItemDTO;
import com.zhku.agriwarningplatform.module.ai.service.dto.AIChatStopReqDTO;
import com.zhku.agriwarningplatform.module.ai.service.dto.AIChatStreamReqDTO;
import com.zhku.agriwarningplatform.module.ai.service.dto.AIChatUpdateTitleReqDTO;
import com.zhku.agriwarningplatform.module.ai.service.dto.AIWarningSuggestionReqDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    /**
     * 悬浮 AI 对话（流式）
     */
    @PostMapping("/assistant/chat/stream")
    public SseEmitter assistantChatStream(@Valid @RequestBody AIAssistantChatStreamParam param,
                                          HttpServletRequest request) {
        log.info("进入接口:AIController#assistantChatStream,param={}", JacksonUtils.writeValueAsString(param));

        validateAssistantChatStreamParam(param);

        Long userId = getCurrentUserId(request);
        AIAssistantChatReqDTO reqDTO = convertToAIAssistantChatReqDTO(param, userId);

        return aiService.assistantChatStream(reqDTO);
    }

    /**
     * 获取悬浮 AI 当前会话历史
     */
    @GetMapping("/assistant/history")
    public CommonResult<List<AIChatMessageVO>> getAssistantHistory(@Valid @ModelAttribute AIAssistantHistoryParam param,
                                                                   HttpServletRequest request) {
        log.info("进入接口:AIController#getAssistantHistory,param={}", JacksonUtils.writeValueAsString(param));

        validateAssistantHistoryParam(param);

        Long userId = getCurrentUserId(request);

        AIChatHistoryQueryDTO queryDTO = new AIChatHistoryQueryDTO();
        queryDTO.setChatId(param.getChatId());
        queryDTO.setUserId(userId);

        List<AIChatMessageDTO> dtoList = aiService.getAssistantHistory(queryDTO);
        return CommonResult.success(convertToAIChatMessageVOList(dtoList));
    }

    /**
     * 独立 AI 问答（流式）
     */
    @PostMapping("/chat/stream")
    public SseEmitter chatStream(@Valid @RequestBody AIChatStreamParam param,
                                 HttpServletRequest request) {
        log.info("进入接口:AIController#chatStream,param={}", JacksonUtils.writeValueAsString(param));

        validateAIChatStreamParam(param);

        Long userId = getCurrentUserId(request);
        AIChatStreamReqDTO reqDTO = convertToAIChatStreamReqDTO(param, userId);

        return aiService.chatStream(reqDTO);
    }

    /**
     * 获取 AI 会话列表
     */
    @GetMapping("/chat/list")
    public CommonResult<List<AIChatSessionItemVO>> getChatSessionList(HttpServletRequest request) {
        log.info("进入接口:AIController#getChatSessionList");

        Long userId = getCurrentUserId(request);
        List<AIChatSessionItemDTO> dtoList = aiService.getChatSessionList(userId);

        return CommonResult.success(convertToAIChatSessionItemVOList(dtoList));
    }

    /**
     * 获取 AI 会话历史
     */
    @GetMapping("/chat/history/{chatId}")
    public CommonResult<List<AIChatMessageVO>> getChatHistory(@Valid @PathVariable("chatId") String chatId,
                                                              HttpServletRequest request) {
        log.info("进入接口:AIController#getChatHistory,chatId={}", chatId);

        if (!StringUtils.hasText(chatId)) {
            throw new ControllerException(AIErrorCode.CHAT_ID_EMPTY);
        }

        Long userId = getCurrentUserId(request);

        AIChatHistoryQueryDTO queryDTO = new AIChatHistoryQueryDTO();
        queryDTO.setChatId(chatId);
        queryDTO.setUserId(userId);

        List<AIChatMessageDTO> dtoList = aiService.getChatHistory(queryDTO);
        return CommonResult.success(convertToAIChatMessageVOList(dtoList));
    }

    /**
     * 获取 AI 会话历史(无chatId抛出异常版)
     * @return
     */
    @GetMapping("/chat/history")
    public CommonResult<Void> getChatHistoryWithoutId() {
        throw new ControllerException(AIErrorCode.CHAT_ID_EMPTY);
    }

    /**
     * 新增 AI 会话
     */
    @PostMapping("/chat/create")
    public CommonResult<Boolean> createChatSession(@Valid @RequestBody AIChatCreateParam param,
                                                   HttpServletRequest request) {
        log.info("进入接口:AIController#createChatSession,param={}", JacksonUtils.writeValueAsString(param));
        if (param.getTitle() != null && param.getTitle().trim().isEmpty()) {
            throw new ControllerException(
                    AIErrorCode.TITLE_EMPTY
            );
        }
        validateAIChatCreateParam(param);

        Long userId = getCurrentUserId(request);
        AIChatCreateReqDTO reqDTO = convertToAIChatCreateReqDTO(param, userId);

        return CommonResult.success(aiService.createChatSession(reqDTO));
    }

    /**
     * 修改 AI 会话标题
     */
    @PutMapping("/chat/updateTitle")
    public CommonResult<Boolean> updateChatTitle(@Valid @RequestBody AIChatUpdateTitleParam param,
                                                 HttpServletRequest request) {
        log.info("进入接口:AIController#updateChatTitle,param={}", JacksonUtils.writeValueAsString(param));
        if (param.getTitle() != null && param.getTitle().trim().isEmpty()) {
            throw new ControllerException(
                    AIErrorCode.TITLE_EMPTY
            );
        }
        validateAIChatUpdateTitleParam(param);

        Long userId = getCurrentUserId(request);
        AIChatUpdateTitleReqDTO reqDTO = convertToAIChatUpdateTitleReqDTO(param, userId);

        return CommonResult.success(aiService.updateChatTitle(reqDTO));
    }

    /**
     * 删除 AI 会话
     */
    @DeleteMapping("/chat/delete/{chatId}")
    public CommonResult<Boolean> deleteChatSession(@Valid @PathVariable("chatId") String chatId,
                                                   HttpServletRequest request) {
        log.info("进入接口:AIController#deleteChatSession,chatId={}", chatId);

        if (!StringUtils.hasText(chatId)) {
            throw new ControllerException(AIErrorCode.CHAT_ID_EMPTY);
        }

        Long userId = getCurrentUserId(request);

        AIChatHistoryQueryDTO queryDTO = new AIChatHistoryQueryDTO();
        queryDTO.setChatId(chatId);
        queryDTO.setUserId(userId);

        return CommonResult.success(aiService.deleteChatSession(queryDTO));
    }

    /**
     * 停止 AI 输出
     */
    @PostMapping("/chat/stop")
    public CommonResult<Boolean> stopChat(@Valid @RequestBody AIChatStopParam param,
                                          HttpServletRequest request) {
        log.info("进入接口:AIController#stopChat,param={}", JacksonUtils.writeValueAsString(param));

        validateAIChatStopParam(param);

        Long userId = getCurrentUserId(request);

        AIChatStopReqDTO reqDTO = new AIChatStopReqDTO();
        reqDTO.setChatId(param.getChatId());
        reqDTO.setUserId(userId);

        return CommonResult.success(aiService.stopChat(reqDTO));
    }

    /**
     * 预警 AI 智能建议（流式）
     */
    @GetMapping("/warnings/{warningId}/suggestion/stream")
    public SseEmitter generateWarningSuggestionStream(@Valid @PathVariable("warningId") Long warningId,
                                                      HttpServletRequest request) {
        log.info("进入接口:AIController#generateWarningSuggestionStream,warningId={}", warningId);

        if (warningId == null || warningId <= 0) {
            throw new ControllerException(AIErrorCode.WARNING_ID_INVALID);
        }

        Long userId = getCurrentUserId(request);

        AIWarningSuggestionReqDTO reqDTO = new AIWarningSuggestionReqDTO();
        reqDTO.setWarningId(warningId);
        reqDTO.setUserId(userId);

        return aiService.generateWarningSuggestionStream(reqDTO);
    }

    // ==================== 参数校验 ====================

    private void validateAssistantChatStreamParam(AIAssistantChatStreamParam param) {
        if (!StringUtils.hasText(param.getChatId())) {
            throw new ControllerException(AIErrorCode.CHAT_ID_EMPTY);
        }
        if (!StringUtils.hasText(param.getPrompt())) {
            throw new ControllerException(AIErrorCode.PROMPT_EMPTY);
        }
        validateContextTypeAndId(param.getContextType(), param.getContextId());
    }

    private void validateAssistantHistoryParam(AIAssistantHistoryParam param) {
        if (!StringUtils.hasText(param.getChatId())) {
            throw new ControllerException(AIErrorCode.CHAT_ID_EMPTY);
        }
    }

    private void validateAIChatStreamParam(AIChatStreamParam param) {
        if (!StringUtils.hasText(param.getChatId())) {
            throw new ControllerException(AIErrorCode.CHAT_ID_EMPTY);
        }
        if (!StringUtils.hasText(param.getPrompt())) {
            throw new ControllerException(AIErrorCode.PROMPT_EMPTY);
        }
    }

    private void validateAIChatCreateParam(AIChatCreateParam param) {
        if (!StringUtils.hasText(param.getChatId())) {
            throw new ControllerException(AIErrorCode.CHAT_ID_EMPTY);
        }
    }

    private void validateAIChatUpdateTitleParam(AIChatUpdateTitleParam param) {
        if (!StringUtils.hasText(param.getChatId())) {
            throw new ControllerException(AIErrorCode.CHAT_ID_EMPTY);
        }
        if (!StringUtils.hasText(param.getTitle())) {
            throw new ControllerException(AIErrorCode.TITLE_EMPTY);
        }
    }

    private void validateAIChatStopParam(AIChatStopParam param) {
        if (!StringUtils.hasText(param.getChatId())) {
            throw new ControllerException(AIErrorCode.CHAT_ID_EMPTY);
        }
    }

    private void validateContextTypeAndId(String contextType, Long contextId) {
        if (!StringUtils.hasText(contextType)) {
            return;
        }
        if (!"CROP".equals(contextType)
                && !"PEST".equals(contextType)
                && !"WARNING".equals(contextType)
                && !"NONE".equals(contextType)) {
            throw new ControllerException(AIErrorCode.CONTEXT_TYPE_INVALID);
        }
        if (!"NONE".equals(contextType) && (contextId == null || contextId <= 0)) {
            throw new ControllerException(AIErrorCode.CONTEXT_ID_INVALID);
        }
    }

    // ==================== DTO转换 ====================

    private AIAssistantChatReqDTO convertToAIAssistantChatReqDTO(AIAssistantChatStreamParam param, Long userId) {
        AIAssistantChatReqDTO reqDTO = new AIAssistantChatReqDTO();
        reqDTO.setChatId(param.getChatId());
        reqDTO.setPrompt(param.getPrompt());
        reqDTO.setContextType(param.getContextType());
        reqDTO.setContextId(param.getContextId());
        reqDTO.setUserId(userId);
        return reqDTO;
    }

    private AIChatStreamReqDTO convertToAIChatStreamReqDTO(AIChatStreamParam param, Long userId) {
        AIChatStreamReqDTO reqDTO = new AIChatStreamReqDTO();
        reqDTO.setChatId(param.getChatId());
        reqDTO.setPrompt(param.getPrompt());
        reqDTO.setUserId(userId);
        return reqDTO;
    }

    private AIChatCreateReqDTO convertToAIChatCreateReqDTO(AIChatCreateParam param, Long userId) {
        AIChatCreateReqDTO reqDTO = new AIChatCreateReqDTO();
        reqDTO.setChatId(param.getChatId());
        reqDTO.setTitle(param.getTitle());
        reqDTO.setUserId(userId);
        return reqDTO;
    }

    private AIChatUpdateTitleReqDTO convertToAIChatUpdateTitleReqDTO(AIChatUpdateTitleParam param, Long userId) {
        AIChatUpdateTitleReqDTO reqDTO = new AIChatUpdateTitleReqDTO();
        reqDTO.setChatId(param.getChatId());
        reqDTO.setTitle(param.getTitle());
        reqDTO.setUserId(userId);
        return reqDTO;
    }

    private List<AIChatMessageVO> convertToAIChatMessageVOList(List<AIChatMessageDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return new ArrayList<>();
        }

        List<AIChatMessageVO> voList = new ArrayList<>();
        for (AIChatMessageDTO dto : dtoList) {
            AIChatMessageVO vo = new AIChatMessageVO();
            vo.setRole(dto.getRole());
            vo.setContent(dto.getContent());
            voList.add(vo);
        }
        return voList;
    }

    private List<AIChatSessionItemVO> convertToAIChatSessionItemVOList(List<AIChatSessionItemDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return new ArrayList<>();
        }

        List<AIChatSessionItemVO> voList = new ArrayList<>();
        for (AIChatSessionItemDTO dto : dtoList) {
            AIChatSessionItemVO vo = new AIChatSessionItemVO();
            vo.setChatId(dto.getChatId());
            vo.setTitle(dto.getTitle());
            voList.add(vo);
        }
        return voList;
    }

    // ==================== Token解析 ====================

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new ControllerException(AIErrorCode.AUTH_HEADER_INVALID);
        }

        String token = authHeader.substring(7);
        Long userId = JwtUtils.getUserIdFromToken(token);

        if (userId == null) {
            throw new ControllerException(AIErrorCode.TOKEN_PARSE_FAILED);
        }

        return userId;
    }
}