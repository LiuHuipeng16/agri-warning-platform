package com.zhku.agriwarningplatform.module.feedback.controller;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:40
 */
import com.zhku.agriwarningplatform.common.errorcode.FeedbackErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.common.util.JacksonUtils;
import com.zhku.agriwarningplatform.common.util.JwtUtils;
import com.zhku.agriwarningplatform.module.feedback.controller.param.FeedbackMyPageParam;
import com.zhku.agriwarningplatform.module.feedback.controller.param.FeedbackPageParam;
import com.zhku.agriwarningplatform.module.feedback.controller.param.FeedbackSubmitParam;
import com.zhku.agriwarningplatform.module.feedback.controller.param.FeedbackUpdateParam;
import com.zhku.agriwarningplatform.module.feedback.controller.vo.FeedbackAIChatTargetDetailVO;
import com.zhku.agriwarningplatform.module.feedback.controller.vo.FeedbackAIImageTargetDetailVO;
import com.zhku.agriwarningplatform.module.feedback.controller.vo.FeedbackAdminPageItemVO;
import com.zhku.agriwarningplatform.module.feedback.controller.vo.FeedbackDetailVO;
import com.zhku.agriwarningplatform.module.feedback.controller.vo.FeedbackPageItemVO;
import com.zhku.agriwarningplatform.module.feedback.controller.vo.FeedbackWarningTargetDetailVO;
import com.zhku.agriwarningplatform.module.feedback.service.FeedbackService;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackAIChatTargetDetailDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackAIImageTargetDetailDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackDeleteDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackDetailDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackPageItemDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackPageQueryDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackSubmitDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackUpdateDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackWarningTargetDetailDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户反馈 Controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private static final String ROLE_ADMIN = "ADMIN";

    private static final String TARGET_TYPE_WARNING = "WARNING";
    private static final String TARGET_TYPE_AI_IMAGE = "AI_IMAGE";
    private static final String TARGET_TYPE_AI_CHAT = "AI_CHAT";

    private static final String FEEDBACK_RESULT_YES = "YES";
    private static final String FEEDBACK_RESULT_NO = "NO";
    private static final String FEEDBACK_RESULT_UNCERTAIN = "UNCERTAIN";

    private final FeedbackService feedbackService;
    private final JwtUtils jwtUtils;

    public FeedbackController(FeedbackService feedbackService,
                              JwtUtils jwtUtils) {
        this.feedbackService = feedbackService;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 提交反馈
     *
     * @param param   提交反馈参数
     * @param request 请求
     * @return 是否提交成功
     */
    @PostMapping("/submit")
    public CommonResult<Boolean> submitFeedback(@Valid @RequestBody FeedbackSubmitParam param,
                                                HttpServletRequest request) {
        log.info("进入接口:FeedbackController#submitFeedback,param={}", JacksonUtils.writeValueAsString(param));

        Long userId = getCurrentUserId(request);
        validateSubmitParam(param);

        FeedbackSubmitDTO submitDTO = convertToFeedbackSubmitDTO(param, userId);
        Boolean result = feedbackService.submitFeedback(submitDTO);

        return CommonResult.success(result);
    }

    /**
     * 获取我的反馈分页列表
     *
     * @param param   查询参数
     * @param request 请求
     * @return 我的反馈分页列表
     */
    @GetMapping("/my/page")
    public CommonResult<PageResult<FeedbackPageItemVO>> getMyFeedbackPage(
            @Valid @ModelAttribute FeedbackMyPageParam param,
            HttpServletRequest request) {
        log.info("进入接口:FeedbackController#getMyFeedbackPage,param={}", JacksonUtils.writeValueAsString(param));

        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);

        validateMyPageParam(param);

        FeedbackPageQueryDTO queryDTO = convertToMyFeedbackPageQueryDTO(param, userId, role);
        PageResult<FeedbackPageItemDTO> pageResult = feedbackService.getMyFeedbackPage(queryDTO);
        PageResult<FeedbackPageItemVO> resultVO = convertToMyFeedbackPageVO(pageResult);

        return CommonResult.success(resultVO);
    }

    /**
     * 获取后台反馈分页列表
     *
     * @param param   查询参数
     * @param request 请求
     * @return 后台反馈分页列表
     */
    @GetMapping("/page")
    public CommonResult<PageResult<FeedbackAdminPageItemVO>> getFeedbackPage(
            @Valid @ModelAttribute FeedbackPageParam param,
            HttpServletRequest request) {
        log.info("进入接口:FeedbackController#getFeedbackPage,param={}", JacksonUtils.writeValueAsString(param));

        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);

        validateAdminRole(role);
        validateFeedbackPageParam(param);

        FeedbackPageQueryDTO queryDTO = convertToFeedbackPageQueryDTO(param, userId, role);
        PageResult<FeedbackPageItemDTO> pageResult = feedbackService.getFeedbackPage(queryDTO);
        PageResult<FeedbackAdminPageItemVO> resultVO = convertToAdminFeedbackPageVO(pageResult);

        return CommonResult.success(resultVO);
    }

    /**
     * 获取反馈详情
     *
     * @param id      反馈ID
     * @param request 请求
     * @return 反馈详情
     */
    @GetMapping("/detail/{id}")
    public CommonResult<FeedbackDetailVO> getFeedbackDetail(
            @PathVariable("id")
            @Min(value = 1, message = "反馈ID必须大于等于1") Long id,
            HttpServletRequest request) {
        log.info("进入接口:FeedbackController#getFeedbackDetail,id={}", id);

        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);

        if (Objects.isNull(id) || id < 1) {
            throw new ControllerException(FeedbackErrorCode.FEEDBACK_ID_INVALID);
        }

        FeedbackDetailDTO detailDTO = feedbackService.getFeedbackDetail(id, userId, role);
        FeedbackDetailVO detailVO = convertToFeedbackDetailVO(detailDTO);

        return CommonResult.success(detailVO);
    }

    /**
     * 编辑反馈
     *
     * @param param   编辑反馈参数
     * @param request 请求
     * @return 是否编辑成功
     */
    @PutMapping("/update")
    public CommonResult<Boolean> updateFeedback(@Valid @RequestBody FeedbackUpdateParam param,
                                                HttpServletRequest request) {
        log.info("进入接口:FeedbackController#updateFeedback,param={}", JacksonUtils.writeValueAsString(param));

        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);

        validateUpdateParam(param);

        FeedbackUpdateDTO updateDTO = convertToFeedbackUpdateDTO(param, userId, role);
        Boolean result = feedbackService.updateFeedback(updateDTO);

        return CommonResult.success(result);
    }

    /**
     * 删除反馈
     *
     * @param id      反馈ID
     * @param request 请求
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{id}")
    public CommonResult<Boolean> deleteFeedback(
            @PathVariable("id")
            @Min(value = 1, message = "反馈ID必须大于等于1") Long id,
            HttpServletRequest request) {
        log.info("进入接口:FeedbackController#deleteFeedback,id={}", id);

        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);

        if (Objects.isNull(id) || id < 1) {
            throw new ControllerException(FeedbackErrorCode.FEEDBACK_ID_INVALID);
        }

        FeedbackDeleteDTO deleteDTO = new FeedbackDeleteDTO();
        deleteDTO.setId(id);
        deleteDTO.setUserId(userId);
        deleteDTO.setRole(role);

        Boolean result = feedbackService.deleteFeedback(deleteDTO);

        return CommonResult.success(result);
    }

    // ==================== private 校验方法 ====================

    private void validateSubmitParam(FeedbackSubmitParam param) {
        if (Objects.isNull(param)) {
            throw new ControllerException(FeedbackErrorCode.TARGET_ID_INVALID);
        }

        if (!isValidTargetType(param.getTargetType())) {
            throw new ControllerException(FeedbackErrorCode.TARGET_TYPE_INVALID);
        }

        if (isBlank(param.getTargetId())) {
            throw new ControllerException(FeedbackErrorCode.TARGET_ID_INVALID);
        }

        if (!isValidFeedbackResult(param.getFeedbackResult())) {
            throw new ControllerException(FeedbackErrorCode.FEEDBACK_RESULT_INVALID);
        }

        if (param.getContent() != null && param.getContent().length() > 500) {
            throw new ControllerException(FeedbackErrorCode.CONTENT_TOO_LONG);
        }
    }

    private void validateMyPageParam(FeedbackMyPageParam param) {
        if (Objects.isNull(param)
                || Objects.isNull(param.getPageNum())
                || Objects.isNull(param.getPageSize())
                || param.getPageNum() < 1
                || param.getPageSize() < 1
                || param.getPageSize() > 50) {
            throw new ControllerException(FeedbackErrorCode.PAGE_PARAM_INVALID);
        }

        if (!isBlank(param.getTargetType()) && !isValidTargetType(param.getTargetType())) {
            throw new ControllerException(FeedbackErrorCode.TARGET_TYPE_INVALID);
        }

        if (!isBlank(param.getFeedbackResult()) && !isValidFeedbackResult(param.getFeedbackResult())) {
            throw new ControllerException(FeedbackErrorCode.FEEDBACK_RESULT_INVALID);
        }
    }

    private void validateFeedbackPageParam(FeedbackPageParam param) {
        if (Objects.isNull(param)
                || Objects.isNull(param.getPageNum())
                || Objects.isNull(param.getPageSize())
                || param.getPageNum() < 1
                || param.getPageSize() < 1
                || param.getPageSize() > 50) {
            throw new ControllerException(FeedbackErrorCode.PAGE_PARAM_INVALID);
        }

        if (!isBlank(param.getTargetType()) && !isValidTargetType(param.getTargetType())) {
            throw new ControllerException(FeedbackErrorCode.TARGET_TYPE_INVALID);
        }

        if (!isBlank(param.getFeedbackResult()) && !isValidFeedbackResult(param.getFeedbackResult())) {
            throw new ControllerException(FeedbackErrorCode.FEEDBACK_RESULT_INVALID);
        }

        if (param.getCropId() != null && param.getCropId() < 1) {
            throw new ControllerException(FeedbackErrorCode.TARGET_ID_INVALID);
        }

        if (param.getPestId() != null && param.getPestId() < 1) {
            throw new ControllerException(FeedbackErrorCode.TARGET_ID_INVALID);
        }

        if (param.getDateStart() != null
                && param.getDateEnd() != null
                && param.getDateStart().isAfter(param.getDateEnd())) {
            throw new ControllerException(FeedbackErrorCode.DATE_RANGE_INVALID);
        }
    }

    private void validateUpdateParam(FeedbackUpdateParam param) {
        if (Objects.isNull(param)) {
            throw new ControllerException(FeedbackErrorCode.FEEDBACK_ID_INVALID);
        }

        if (Objects.isNull(param.getId()) || param.getId() < 1) {
            throw new ControllerException(FeedbackErrorCode.FEEDBACK_ID_INVALID);
        }

        if (!isValidFeedbackResult(param.getFeedbackResult())) {
            throw new ControllerException(FeedbackErrorCode.FEEDBACK_RESULT_INVALID);
        }

        if (param.getContent() != null && param.getContent().length() > 500) {
            throw new ControllerException(FeedbackErrorCode.CONTENT_TOO_LONG);
        }
    }

    private void validateAdminRole(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new ControllerException(FeedbackErrorCode.ADMIN_PERMISSION_REQUIRED);
        }
    }

    private boolean isValidTargetType(String targetType) {
        return TARGET_TYPE_WARNING.equals(targetType)
                || TARGET_TYPE_AI_IMAGE.equals(targetType)
                || TARGET_TYPE_AI_CHAT.equals(targetType);
    }

    private boolean isValidFeedbackResult(String feedbackResult) {
        return FEEDBACK_RESULT_YES.equals(feedbackResult)
                || FEEDBACK_RESULT_NO.equals(feedbackResult)
                || FEEDBACK_RESULT_UNCERTAIN.equals(feedbackResult);
    }

    // ==================== private DTO转换方法 ====================

    private FeedbackSubmitDTO convertToFeedbackSubmitDTO(FeedbackSubmitParam param, Long userId) {
        FeedbackSubmitDTO submitDTO = new FeedbackSubmitDTO();
        submitDTO.setUserId(userId);
        submitDTO.setTargetType(trimToNull(param.getTargetType()));
        submitDTO.setTargetId(trimToNull(param.getTargetId()));
        submitDTO.setFeedbackResult(trimToNull(param.getFeedbackResult()));
        submitDTO.setContent(trimToNull(param.getContent()));
        return submitDTO;
    }

    private FeedbackPageQueryDTO convertToMyFeedbackPageQueryDTO(FeedbackMyPageParam param,
                                                                 Long userId,
                                                                 String role) {
        FeedbackPageQueryDTO queryDTO = new FeedbackPageQueryDTO();
        queryDTO.setPageNum(param.getPageNum());
        queryDTO.setPageSize(param.getPageSize());
        queryDTO.setUserId(userId);
        queryDTO.setRole(role);
        queryDTO.setTargetType(trimToNull(param.getTargetType()));
        queryDTO.setFeedbackResult(trimToNull(param.getFeedbackResult()));
        return queryDTO;
    }

    private FeedbackPageQueryDTO convertToFeedbackPageQueryDTO(FeedbackPageParam param,
                                                               Long userId,
                                                               String role) {
        FeedbackPageQueryDTO queryDTO = new FeedbackPageQueryDTO();
        queryDTO.setPageNum(param.getPageNum());
        queryDTO.setPageSize(param.getPageSize());
        queryDTO.setUserId(userId);
        queryDTO.setRole(role);
        queryDTO.setUsername(trimToNull(param.getUsername()));
        queryDTO.setTargetType(trimToNull(param.getTargetType()));
        queryDTO.setFeedbackResult(trimToNull(param.getFeedbackResult()));
        queryDTO.setCropId(param.getCropId());
        queryDTO.setPestId(param.getPestId());
        queryDTO.setDateStart(param.getDateStart());
        queryDTO.setDateEnd(param.getDateEnd());
        return queryDTO;
    }

    private FeedbackUpdateDTO convertToFeedbackUpdateDTO(FeedbackUpdateParam param,
                                                         Long userId,
                                                         String role) {
        FeedbackUpdateDTO updateDTO = new FeedbackUpdateDTO();
        updateDTO.setId(param.getId());
        updateDTO.setUserId(userId);
        updateDTO.setRole(role);
        updateDTO.setFeedbackResult(trimToNull(param.getFeedbackResult()));
        updateDTO.setContent(trimToNull(param.getContent()));
        return updateDTO;
    }

    // ==================== private VO转换方法 ====================

    private PageResult<FeedbackPageItemVO> convertToMyFeedbackPageVO(PageResult<FeedbackPageItemDTO> pageResult) {
        List<FeedbackPageItemVO> records = new ArrayList<>();
        if (pageResult != null && pageResult.getRecords() != null) {
            for (FeedbackPageItemDTO itemDTO : pageResult.getRecords()) {
                FeedbackPageItemVO itemVO = new FeedbackPageItemVO();
                itemVO.setId(itemDTO.getId());
                itemVO.setTargetType(itemDTO.getTargetType());
                itemVO.setTargetId(itemDTO.getTargetId());
                itemVO.setFeedbackResult(itemDTO.getFeedbackResult());
                itemVO.setContent(itemDTO.getContent());
                itemVO.setGmtCreate(itemDTO.getGmtCreate());
                records.add(itemVO);
            }
        }

        Integer total = pageResult == null || pageResult.getTotal() == null ? 0 : pageResult.getTotal();
        return new PageResult<>(total, records);
    }

    private PageResult<FeedbackAdminPageItemVO> convertToAdminFeedbackPageVO(PageResult<FeedbackPageItemDTO> pageResult) {
        List<FeedbackAdminPageItemVO> records = new ArrayList<>();
        if (pageResult != null && pageResult.getRecords() != null) {
            for (FeedbackPageItemDTO itemDTO : pageResult.getRecords()) {
                FeedbackAdminPageItemVO itemVO = new FeedbackAdminPageItemVO();
                itemVO.setId(itemDTO.getId());
                itemVO.setUserId(itemDTO.getUserId());
                itemVO.setUsername(itemDTO.getUsername());
                itemVO.setTargetType(itemDTO.getTargetType());
                itemVO.setTargetId(itemDTO.getTargetId());
                itemVO.setFeedbackResult(itemDTO.getFeedbackResult());
                itemVO.setContent(itemDTO.getContent());
                itemVO.setGmtCreate(itemDTO.getGmtCreate());
                records.add(itemVO);
            }
        }

        Integer total = pageResult == null || pageResult.getTotal() == null ? 0 : pageResult.getTotal();
        return new PageResult<>(total, records);
    }

    private FeedbackDetailVO convertToFeedbackDetailVO(FeedbackDetailDTO detailDTO) {
        FeedbackDetailVO detailVO = new FeedbackDetailVO();
        detailVO.setId(detailDTO.getId());
        detailVO.setUserId(detailDTO.getUserId());
        detailVO.setUsername(detailDTO.getUsername());
        detailVO.setTargetType(detailDTO.getTargetType());
        detailVO.setTargetId(detailDTO.getTargetId());
        detailVO.setFeedbackResult(detailDTO.getFeedbackResult());
        detailVO.setContent(detailDTO.getContent());
        detailVO.setGmtCreate(detailDTO.getGmtCreate());
        detailVO.setTargetDetail(convertTargetDetailVO(detailDTO.getTargetType(), detailDTO.getTargetDetail()));
        return detailVO;
    }

    private Object convertTargetDetailVO(String targetType, Object targetDetail) {
        if (targetDetail == null) {
            return null;
        }

        if (TARGET_TYPE_WARNING.equals(targetType)
                && targetDetail instanceof FeedbackWarningTargetDetailDTO detailDTO) {
            FeedbackWarningTargetDetailVO detailVO = new FeedbackWarningTargetDetailVO();
            detailVO.setWarningId(detailDTO.getWarningId());
            detailVO.setTitle(detailDTO.getTitle());
            detailVO.setCropName(detailDTO.getCropName());
            detailVO.setPestName(detailDTO.getPestName());
            detailVO.setWarningDate(detailDTO.getWarningDate());
            detailVO.setWarningType(detailDTO.getWarningType());
            detailVO.setRiskLevel(detailDTO.getRiskLevel());
            detailVO.setRiskScore(detailDTO.getRiskScore());
            return detailVO;
        }

        if (TARGET_TYPE_AI_IMAGE.equals(targetType)
                && targetDetail instanceof FeedbackAIImageTargetDetailDTO detailDTO) {
            FeedbackAIImageTargetDetailVO detailVO = new FeedbackAIImageTargetDetailVO();
            detailVO.setChatId(detailDTO.getChatId());
            detailVO.setQuestion(detailDTO.getQuestion());
            detailVO.setImageAnalysis(detailDTO.getImageAnalysis());
            detailVO.setAnswerSummary(detailDTO.getAnswerSummary());
            return detailVO;
        }

        if (TARGET_TYPE_AI_CHAT.equals(targetType)
                && targetDetail instanceof FeedbackAIChatTargetDetailDTO detailDTO) {
            FeedbackAIChatTargetDetailVO detailVO = new FeedbackAIChatTargetDetailVO();
            detailVO.setChatId(detailDTO.getChatId());
            detailVO.setQuestion(detailDTO.getQuestion());
            detailVO.setAnswerSummary(detailDTO.getAnswerSummary());
            return detailVO;
        }

        return targetDetail;
    }

    // ==================== private 登录信息方法 ====================

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getToken(request);
        Long userId = jwtUtils.getUserIdFromToken(token);
        if (Objects.isNull(userId) || userId < 1) {
            throw new ControllerException(FeedbackErrorCode.USER_NOT_LOGIN);
        }
        return userId;
    }

    private String getCurrentUserRole(HttpServletRequest request) {
        String token = getToken(request);
        String role = jwtUtils.getRoleFromToken(token);
        if (isBlank(role)) {
            throw new ControllerException(FeedbackErrorCode.USER_NOT_LOGIN);
        }
        return role;
    }

    private String getToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            throw new ControllerException(FeedbackErrorCode.USER_NOT_LOGIN);
        }
        return authorization.substring(7);
    }

    // ==================== private 工具方法 ====================

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimValue = value.trim();
        return trimValue.isEmpty() ? null : trimValue;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
