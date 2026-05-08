package com.zhku.agriwarningplatform.module.feedback.service.impl;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:40
 */
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zhku.agriwarningplatform.common.errorcode.FeedbackErrorCode;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.feedback.mapper.FeedbackMapper;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackAIMessageTargetDetailDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackDetailDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackPageItemDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackPageQueryDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackWarningTargetDetailDO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户反馈 Service 实现类
 */
@Slf4j
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final String ROLE_ADMIN = "ADMIN";

    private static final String TARGET_TYPE_WARNING = "WARNING";
    private static final String TARGET_TYPE_AI_IMAGE = "AI_IMAGE";
    private static final String TARGET_TYPE_AI_CHAT = "AI_CHAT";

    private static final String FEEDBACK_RESULT_YES = "YES";
    private static final String FEEDBACK_RESULT_NO = "NO";
    private static final String FEEDBACK_RESULT_UNCERTAIN = "UNCERTAIN";

    private static final String MESSAGE_TYPE_TEXT = "TEXT";
    private static final String MESSAGE_TYPE_IMAGE_TEXT = "IMAGE_TEXT";

    private final FeedbackMapper feedbackMapper;

    public FeedbackServiceImpl(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitFeedback(FeedbackSubmitDTO submitDTO) {
        try {
            validateSubmitDTO(submitDTO);

            validateFeedbackTargetExists(
                    submitDTO.getTargetType(),
                    submitDTO.getTargetId()
            );

            FeedbackDO feedbackDO = new FeedbackDO();
            feedbackDO.setUserId(submitDTO.getUserId());
            feedbackDO.setTargetType(trimToNull(submitDTO.getTargetType()));
            feedbackDO.setTargetId(trimToNull(submitDTO.getTargetId()));
            feedbackDO.setFeedbackResult(trimToNull(submitDTO.getFeedbackResult()));
            feedbackDO.setContent(trimToNull(submitDTO.getContent()));
            feedbackDO.setDeleteFlag(0);

            int rows = feedbackMapper.insert(feedbackDO);
            if (rows != 1) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_CREATE_FAILED);
            }

            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("提交反馈异常，submitDTO={}", submitDTO, e);
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_CREATE_FAILED);
        }
    }

    @Override
    public PageResult<FeedbackPageItemDTO> getMyFeedbackPage(FeedbackPageQueryDTO queryDTO) {
        try {
            validateMyPageQueryDTO(queryDTO);

            FeedbackPageQueryDO queryDO = buildFeedbackPageQueryDO(queryDTO);

            Integer total = feedbackMapper.countMyFeedbackPage(queryDO);
            if (Objects.isNull(total) || total <= 0) {
                return new PageResult<>(0, new ArrayList<>());
            }

            List<FeedbackPageItemDO> itemDOList = feedbackMapper.selectMyFeedbackPage(queryDO);
            List<FeedbackPageItemDTO> records = convertToFeedbackPageItemDTOList(itemDOList);

            return new PageResult<>(total, records);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询我的反馈分页异常，queryDTO={}", queryDTO, e);
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_PAGE_QUERY_FAILED);
        }
    }

    @Override
    public PageResult<FeedbackPageItemDTO> getFeedbackPage(FeedbackPageQueryDTO queryDTO) {
        try {
            validateAdminPageQueryDTO(queryDTO);

            FeedbackPageQueryDO queryDO = buildFeedbackPageQueryDO(queryDTO);

            Integer total = feedbackMapper.countFeedbackPage(queryDO);
            if (Objects.isNull(total) || total <= 0) {
                return new PageResult<>(0, new ArrayList<>());
            }

            List<FeedbackPageItemDO> itemDOList = feedbackMapper.selectFeedbackPage(queryDO);
            List<FeedbackPageItemDTO> records = convertToFeedbackPageItemDTOList(itemDOList);

            return new PageResult<>(total, records);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询后台反馈分页异常，queryDTO={}", queryDTO, e);
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_PAGE_QUERY_FAILED);
        }
    }

    @Override
    public FeedbackDetailDTO getFeedbackDetail(Long id, Long userId, String role) {
        try {
            if (Objects.isNull(id) || id < 1) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_ID_INVALID);
            }

            if (Objects.isNull(userId) || userId < 1) {
                throw new ServiceException(FeedbackErrorCode.USER_NOT_LOGIN);
            }

            FeedbackDetailDO detailDO = feedbackMapper.selectFeedbackDetailById(id);
            if (Objects.isNull(detailDO)) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_NOT_EXIST);
            }

            checkFeedbackPermission(detailDO.getUserId(), userId, role);

            FeedbackDetailDTO detailDTO = convertToFeedbackDetailDTO(detailDO);
            detailDTO.setTargetDetail(buildTargetDetail(
                    detailDO.getTargetType(),
                    detailDO.getTargetId()
            ));

            return detailDTO;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询反馈详情异常，id={}, userId={}, role={}", id, userId, role, e);
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_DETAIL_QUERY_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFeedback(FeedbackUpdateDTO updateDTO) {
        try {
            validateUpdateDTO(updateDTO);

            FeedbackDO oldFeedbackDO = feedbackMapper.selectValidFeedbackById(updateDTO.getId());
            if (Objects.isNull(oldFeedbackDO)) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_NOT_EXIST);
            }

            checkFeedbackPermission(oldFeedbackDO.getUserId(), updateDTO.getUserId(), updateDTO.getRole());

            FeedbackDO updateDO = new FeedbackDO();
            updateDO.setId(updateDTO.getId());
            updateDO.setFeedbackResult(trimToNull(updateDTO.getFeedbackResult()));
            updateDO.setContent(trimToNull(updateDTO.getContent()));

            int rows = feedbackMapper.updateFeedbackSelective(updateDO);
            if (rows != 1) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_UPDATE_FAILED);
            }

            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("编辑反馈异常，updateDTO={}", updateDTO, e);
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_UPDATE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFeedback(FeedbackDeleteDTO deleteDTO) {
        try {
            validateDeleteDTO(deleteDTO);

            FeedbackDO feedbackDO = feedbackMapper.selectValidFeedbackById(deleteDTO.getId());
            if (Objects.isNull(feedbackDO)) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_NOT_EXIST);
            }

            checkFeedbackPermission(feedbackDO.getUserId(), deleteDTO.getUserId(), deleteDTO.getRole());

            int rows = feedbackMapper.deleteFeedbackLogical(deleteDTO.getId());
            if (rows != 1) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_DELETE_FAILED);
            }

            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除反馈异常，deleteDTO={}", deleteDTO, e);
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_DELETE_FAILED);
        }
    }

    // ==================== private 校验方法 ====================

    private void validateSubmitDTO(FeedbackSubmitDTO submitDTO) {
        if (Objects.isNull(submitDTO)) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_CREATE_FAILED);
        }

        if (Objects.isNull(submitDTO.getUserId()) || submitDTO.getUserId() < 1) {
            throw new ServiceException(FeedbackErrorCode.USER_NOT_LOGIN);
        }

        if (!isValidTargetType(submitDTO.getTargetType())) {
            throw new ServiceException(FeedbackErrorCode.TARGET_TYPE_INVALID);
        }

        if (StringUtils.isBlank(submitDTO.getTargetId())) {
            throw new ServiceException(FeedbackErrorCode.TARGET_ID_INVALID);
        }

        if (!isValidFeedbackResult(submitDTO.getFeedbackResult())) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_RESULT_INVALID);
        }

        if (submitDTO.getContent() != null && submitDTO.getContent().length() > 500) {
            throw new ServiceException(FeedbackErrorCode.CONTENT_TOO_LONG);
        }
    }

    private void validateMyPageQueryDTO(FeedbackPageQueryDTO queryDTO) {
        validateBasePageQueryDTO(queryDTO);

        if (Objects.isNull(queryDTO.getUserId()) || queryDTO.getUserId() < 1) {
            throw new ServiceException(FeedbackErrorCode.USER_NOT_LOGIN);
        }
    }

    private void validateAdminPageQueryDTO(FeedbackPageQueryDTO queryDTO) {
        validateBasePageQueryDTO(queryDTO);

        if (!isAdmin(queryDTO.getRole())) {
            throw new ServiceException(FeedbackErrorCode.ADMIN_PERMISSION_REQUIRED);
        }

        if (queryDTO.getDateStart() != null
                && queryDTO.getDateEnd() != null
                && queryDTO.getDateStart().isAfter(queryDTO.getDateEnd())) {
            throw new ServiceException(FeedbackErrorCode.DATE_RANGE_INVALID);
        }
    }

    private void validateBasePageQueryDTO(FeedbackPageQueryDTO queryDTO) {
        if (Objects.isNull(queryDTO)
                || Objects.isNull(queryDTO.getPageNum())
                || Objects.isNull(queryDTO.getPageSize())
                || queryDTO.getPageNum() < 1
                || queryDTO.getPageSize() < 1
                || queryDTO.getPageSize() > 50) {
            throw new ServiceException(FeedbackErrorCode.PAGE_PARAM_INVALID);
        }

        if (StringUtils.isNotBlank(queryDTO.getTargetType())
                && !isValidTargetType(queryDTO.getTargetType())) {
            throw new ServiceException(FeedbackErrorCode.TARGET_TYPE_INVALID);
        }

        if (StringUtils.isNotBlank(queryDTO.getFeedbackResult())
                && !isValidFeedbackResult(queryDTO.getFeedbackResult())) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_RESULT_INVALID);
        }
    }

    private void validateUpdateDTO(FeedbackUpdateDTO updateDTO) {
        if (Objects.isNull(updateDTO)) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_UPDATE_FAILED);
        }

        if (Objects.isNull(updateDTO.getId()) || updateDTO.getId() < 1) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_ID_INVALID);
        }

        if (Objects.isNull(updateDTO.getUserId()) || updateDTO.getUserId() < 1) {
            throw new ServiceException(FeedbackErrorCode.USER_NOT_LOGIN);
        }

        if (!isValidFeedbackResult(updateDTO.getFeedbackResult())) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_RESULT_INVALID);
        }

        if (updateDTO.getContent() != null && updateDTO.getContent().length() > 500) {
            throw new ServiceException(FeedbackErrorCode.CONTENT_TOO_LONG);
        }
    }

    private void validateDeleteDTO(FeedbackDeleteDTO deleteDTO) {
        if (Objects.isNull(deleteDTO)) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_DELETE_FAILED);
        }

        if (Objects.isNull(deleteDTO.getId()) || deleteDTO.getId() < 1) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_ID_INVALID);
        }

        if (Objects.isNull(deleteDTO.getUserId()) || deleteDTO.getUserId() < 1) {
            throw new ServiceException(FeedbackErrorCode.USER_NOT_LOGIN);
        }
    }

    private void validateFeedbackTargetExists(String targetType, String targetId) {
        if (TARGET_TYPE_WARNING.equals(targetType)) {
            Long warningId = parseLongTargetId(targetId, FeedbackErrorCode.WARNING_TARGET_ID_INVALID);
            Integer count = feedbackMapper.countWarningTargetById(warningId);
            if (Objects.isNull(count) || count <= 0) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_TARGET_NOT_EXIST);
            }
            return;
        }

        if (TARGET_TYPE_AI_CHAT.equals(targetType)) {
            Long messageId = parseLongTargetId(targetId, FeedbackErrorCode.AI_TARGET_ID_INVALID);
            Integer count = feedbackMapper.countAIMessageTargetById(messageId, MESSAGE_TYPE_TEXT);
            if (Objects.isNull(count) || count <= 0) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_TARGET_NOT_EXIST);
            }
            return;
        }

        if (TARGET_TYPE_AI_IMAGE.equals(targetType)) {
            Long messageId = parseLongTargetId(targetId, FeedbackErrorCode.AI_TARGET_ID_INVALID);
            Integer count = feedbackMapper.countAIMessageTargetById(messageId, MESSAGE_TYPE_IMAGE_TEXT);
            if (Objects.isNull(count) || count <= 0) {
                throw new ServiceException(FeedbackErrorCode.FEEDBACK_TARGET_NOT_EXIST);
            }
            return;
        }

        throw new ServiceException(FeedbackErrorCode.TARGET_TYPE_INVALID);
    }

    private void checkFeedbackPermission(Long feedbackUserId, Long currentUserId, String role) {
        if (isAdmin(role)) {
            return;
        }

        if (!Objects.equals(feedbackUserId, currentUserId)) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_OPERATION_FORBIDDEN);
        }
    }

    // ==================== private 目标详情 ====================

    private Object buildTargetDetail(String targetType, String targetId) {
        try {
            if (TARGET_TYPE_WARNING.equals(targetType)) {
                return buildWarningTargetDetail(targetId);
            }

            if (TARGET_TYPE_AI_IMAGE.equals(targetType)) {
                return buildAIImageTargetDetail(targetId);
            }

            if (TARGET_TYPE_AI_CHAT.equals(targetType)) {
                return buildAIChatTargetDetail(targetId);
            }

            throw new ServiceException(FeedbackErrorCode.TARGET_TYPE_INVALID);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询反馈目标摘要异常，targetType={}, targetId={}", targetType, targetId, e);
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_TARGET_DETAIL_QUERY_FAILED);
        }
    }

    private FeedbackWarningTargetDetailDTO buildWarningTargetDetail(String targetId) {
        Long warningId = parseLongTargetId(targetId, FeedbackErrorCode.WARNING_TARGET_ID_INVALID);

        FeedbackWarningTargetDetailDO detailDO = feedbackMapper.selectWarningTargetDetail(warningId);
        if (Objects.isNull(detailDO)) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_TARGET_NOT_EXIST);
        }

        FeedbackWarningTargetDetailDTO detailDTO = new FeedbackWarningTargetDetailDTO();
        detailDTO.setWarningId(detailDO.getWarningId());
        detailDTO.setTitle(detailDO.getTitle());
        detailDTO.setCropName(detailDO.getCropName());
        detailDTO.setPestName(detailDO.getPestName());
        detailDTO.setWarningDate(detailDO.getWarningDate());
        detailDTO.setWarningType(detailDO.getWarningType());
        detailDTO.setRiskLevel(detailDO.getRiskLevel());
        detailDTO.setRiskScore(detailDO.getRiskScore());
        return detailDTO;
    }

    private FeedbackAIImageTargetDetailDTO buildAIImageTargetDetail(String targetId) {
        Long messageId = parseLongTargetId(targetId, FeedbackErrorCode.AI_TARGET_ID_INVALID);

        FeedbackAIMessageTargetDetailDO detailDO =
                feedbackMapper.selectAIAnswerMessageDetail(messageId, MESSAGE_TYPE_IMAGE_TEXT);

        if (Objects.isNull(detailDO)) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_TARGET_NOT_EXIST);
        }

        String question = feedbackMapper.selectPreviousUserQuestion(detailDO.getChatId(), messageId);

        FeedbackAIImageTargetDetailDTO detailDTO = new FeedbackAIImageTargetDetailDTO();
        detailDTO.setChatId(detailDO.getChatId());
        detailDTO.setQuestion(emptyIfNull(question));
        detailDTO.setImageAnalysis(detailDO.getImageAnalysis());
        detailDTO.setAnswerSummary(emptyIfNull(detailDO.getAnswerSummary()));
        return detailDTO;
    }

    private FeedbackAIChatTargetDetailDTO buildAIChatTargetDetail(String targetId) {
        Long messageId = parseLongTargetId(targetId, FeedbackErrorCode.AI_TARGET_ID_INVALID);

        FeedbackAIMessageTargetDetailDO detailDO =
                feedbackMapper.selectAIAnswerMessageDetail(messageId, MESSAGE_TYPE_TEXT);

        if (Objects.isNull(detailDO)) {
            throw new ServiceException(FeedbackErrorCode.FEEDBACK_TARGET_NOT_EXIST);
        }

        String question = feedbackMapper.selectPreviousUserQuestion(detailDO.getChatId(), messageId);

        FeedbackAIChatTargetDetailDTO detailDTO = new FeedbackAIChatTargetDetailDTO();
        detailDTO.setChatId(detailDO.getChatId());
        detailDTO.setQuestion(emptyIfNull(question));
        detailDTO.setAnswerSummary(emptyIfNull(detailDO.getAnswerSummary()));
        return detailDTO;
    }

    // ==================== private 转换方法 ====================

    private FeedbackPageQueryDO buildFeedbackPageQueryDO(FeedbackPageQueryDTO queryDTO) {
        FeedbackPageQueryDO queryDO = new FeedbackPageQueryDO();
        queryDO.setUserId(queryDTO.getUserId());
        queryDO.setUsername(trimToNull(queryDTO.getUsername()));
        queryDO.setTargetType(trimToNull(queryDTO.getTargetType()));
        queryDO.setFeedbackResult(trimToNull(queryDTO.getFeedbackResult()));
        queryDO.setCropId(queryDTO.getCropId());
        queryDO.setPestId(queryDTO.getPestId());
        queryDO.setDateStart(queryDTO.getDateStart());
        queryDO.setDateEnd(queryDTO.getDateEnd());
        queryDO.setOffset((queryDTO.getPageNum() - 1) * queryDTO.getPageSize());
        queryDO.setPageSize(queryDTO.getPageSize());
        return queryDO;
    }

    private List<FeedbackPageItemDTO> convertToFeedbackPageItemDTOList(List<FeedbackPageItemDO> itemDOList) {
        List<FeedbackPageItemDTO> result = new ArrayList<>();
        if (itemDOList == null || itemDOList.isEmpty()) {
            return result;
        }

        for (FeedbackPageItemDO itemDO : itemDOList) {
            FeedbackPageItemDTO itemDTO = new FeedbackPageItemDTO();
            itemDTO.setId(itemDO.getId());
            itemDTO.setUserId(itemDO.getUserId());
            itemDTO.setUsername(itemDO.getUsername());
            itemDTO.setTargetType(itemDO.getTargetType());
            itemDTO.setTargetId(itemDO.getTargetId());
            itemDTO.setFeedbackResult(itemDO.getFeedbackResult());
            itemDTO.setContent(itemDO.getContent());
            itemDTO.setGmtCreate(itemDO.getGmtCreate());
            result.add(itemDTO);
        }
        return result;
    }

    private FeedbackDetailDTO convertToFeedbackDetailDTO(FeedbackDetailDO detailDO) {
        FeedbackDetailDTO detailDTO = new FeedbackDetailDTO();
        detailDTO.setId(detailDO.getId());
        detailDTO.setUserId(detailDO.getUserId());
        detailDTO.setUsername(detailDO.getUsername());
        detailDTO.setTargetType(detailDO.getTargetType());
        detailDTO.setTargetId(detailDO.getTargetId());
        detailDTO.setFeedbackResult(detailDO.getFeedbackResult());
        detailDTO.setContent(detailDO.getContent());
        detailDTO.setGmtCreate(detailDO.getGmtCreate());
        return detailDTO;
    }

    // ==================== private 工具方法 ====================

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

    private boolean isAdmin(String role) {
        return ROLE_ADMIN.equals(role);
    }

    private Long parseLongTargetId(String targetId, Object errorCodeObj) {
        try {
            if (StringUtils.isBlank(targetId)) {
                throw new ServiceException(FeedbackErrorCode.TARGET_ID_INVALID);
            }

            Long value = Long.valueOf(targetId);
            if (value < 1) {
                throw new NumberFormatException();
            }
            return value;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            if (FeedbackErrorCode.WARNING_TARGET_ID_INVALID.equals(errorCodeObj)) {
                throw new ServiceException(FeedbackErrorCode.WARNING_TARGET_ID_INVALID);
            }
            if (FeedbackErrorCode.AI_TARGET_ID_INVALID.equals(errorCodeObj)) {
                throw new ServiceException(FeedbackErrorCode.AI_TARGET_ID_INVALID);
            }
            throw new ServiceException(FeedbackErrorCode.TARGET_ID_INVALID);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimValue = value.trim();
        return trimValue.isEmpty() ? null : trimValue;
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}