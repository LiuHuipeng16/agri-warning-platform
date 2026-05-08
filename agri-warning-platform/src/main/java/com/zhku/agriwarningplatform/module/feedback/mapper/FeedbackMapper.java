package com.zhku.agriwarningplatform.module.feedback.mapper;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:38
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackAIMessageTargetDetailDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackDetailDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackPageItemDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackPageQueryDO;
import com.zhku.agriwarningplatform.module.feedback.mapper.dataobject.FeedbackWarningTargetDetailDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户反馈 Mapper
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<FeedbackDO> {

    /**
     * 查询我的反馈分页总数
     *
     * @param queryDO 查询条件
     * @return 总数
     */
    Integer countMyFeedbackPage(@Param("query") FeedbackPageQueryDO queryDO);

    /**
     * 查询我的反馈分页列表
     *
     * @param queryDO 查询条件
     * @return 分页列表
     */
    List<FeedbackPageItemDO> selectMyFeedbackPage(@Param("query") FeedbackPageQueryDO queryDO);

    /**
     * 查询后台反馈分页总数
     *
     * @param queryDO 查询条件
     * @return 总数
     */
    Integer countFeedbackPage(@Param("query") FeedbackPageQueryDO queryDO);

    /**
     * 查询后台反馈分页列表
     *
     * @param queryDO 查询条件
     * @return 分页列表
     */
    List<FeedbackPageItemDO> selectFeedbackPage(@Param("query") FeedbackPageQueryDO queryDO);

    /**
     * 根据反馈ID查询详情
     *
     * @param id 反馈ID
     * @return 反馈详情
     */
    FeedbackDetailDO selectFeedbackDetailById(@Param("id") Long id);

    /**
     * 根据反馈ID查询有效反馈
     *
     * @param id 反馈ID
     * @return 反馈DO
     */
    FeedbackDO selectValidFeedbackById(@Param("id") Long id);

    /**
     * 逻辑删除反馈
     *
     * @param id 反馈ID
     * @return 影响行数
     */
    int deleteFeedbackLogical(@Param("id") Long id);

    /**
     * 查询预警反馈目标是否存在
     *
     * @param warningId 预警ID
     * @return 数量
     */
    Integer countWarningTargetById(@Param("warningId") Long warningId);

    /**
     * 查询AI反馈目标消息是否存在
     *
     * @param messageId AI消息ID
     * @param messageType 消息类型
     * @return 数量
     */
    Integer countAIMessageTargetById(@Param("messageId") Long messageId,
                                     @Param("messageType") String messageType);

    /**
     * 查询预警反馈目标摘要
     *
     * @param warningId 预警ID
     * @return 预警摘要
     */
    FeedbackWarningTargetDetailDO selectWarningTargetDetail(@Param("warningId") Long warningId);

    /**
     * 查询AI回答消息摘要
     *
     * @param messageId assistant消息ID
     * @param messageType 消息类型
     * @return AI回答摘要
     */
    FeedbackAIMessageTargetDetailDO selectAIAnswerMessageDetail(@Param("messageId") Long messageId,
                                                                @Param("messageType") String messageType);

    /**
     * 查询AI回答前最近一条用户提问
     *
     * @param chatId 会话ID
     * @param beforeMessageId assistant消息ID
     * @return 用户提问内容
     */
    String selectPreviousUserQuestion(@Param("chatId") String chatId,
                                      @Param("beforeMessageId") Long beforeMessageId);

    /**
     * 动态修改反馈
     *
     * @param feedbackDO 反馈DO
     * @return 影响行数
     */
    int updateFeedbackSelective(FeedbackDO feedbackDO);
}