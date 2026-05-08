package com.zhku.agriwarningplatform.module.feedback.service;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:39
 */
import com.zhku.agriwarningplatform.common.result.PageResult;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackDeleteDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackDetailDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackPageItemDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackPageQueryDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackSubmitDTO;
import com.zhku.agriwarningplatform.module.feedback.service.dto.FeedbackUpdateDTO;

/**
 * 用户反馈 Service
 */
public interface FeedbackService {

    /**
     * 提交反馈
     *
     * @param submitDTO 提交反馈DTO
     * @return 是否提交成功
     */
    Boolean submitFeedback(FeedbackSubmitDTO submitDTO);

    /**
     * 获取我的反馈分页列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<FeedbackPageItemDTO> getMyFeedbackPage(FeedbackPageQueryDTO queryDTO);

    /**
     * 获取后台反馈分页列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<FeedbackPageItemDTO> getFeedbackPage(FeedbackPageQueryDTO queryDTO);

    /**
     * 获取反馈详情
     *
     * @param id     反馈ID
     * @param userId 当前登录用户ID
     * @param role   当前登录用户角色
     * @return 反馈详情
     */
    FeedbackDetailDTO getFeedbackDetail(Long id, Long userId, String role);

    /**
     * 编辑反馈
     *
     * @param updateDTO 编辑反馈DTO
     * @return 是否修改成功
     */
    Boolean updateFeedback(FeedbackUpdateDTO updateDTO);

    /**
     * 删除反馈
     *
     * @param deleteDTO 删除反馈DTO
     * @return 是否删除成功
     */
    Boolean deleteFeedback(FeedbackDeleteDTO deleteDTO);
}
