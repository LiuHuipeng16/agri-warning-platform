package com.zhku.agriwarningplatform.common.errorcode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-05-08
 * Time: 17:37
 */
/**
 * 用户反馈模块错误码
 */
public interface FeedbackErrorCode {

    // ==================== controller层错误码 ====================

    ErrorCode PAGE_PARAM_INVALID =
            new ErrorCode(400, "FEEDBACK_001", "分页参数不合法");

    ErrorCode FEEDBACK_ID_INVALID =
            new ErrorCode(400, "FEEDBACK_002", "反馈ID不合法");

    ErrorCode TARGET_TYPE_INVALID =
            new ErrorCode(400, "FEEDBACK_003", "反馈目标类型不合法");

    ErrorCode TARGET_ID_INVALID =
            new ErrorCode(400, "FEEDBACK_004", "反馈目标ID不合法");

    ErrorCode FEEDBACK_RESULT_INVALID =
            new ErrorCode(400, "FEEDBACK_005", "反馈结果不合法");

    ErrorCode CONTENT_TOO_LONG =
            new ErrorCode(400, "FEEDBACK_006", "反馈补充说明不能超过500个字符");

    ErrorCode DATE_RANGE_INVALID =
            new ErrorCode(400, "FEEDBACK_007", "反馈日期范围不合法");

    ErrorCode USER_NOT_LOGIN =
            new ErrorCode(401, "FEEDBACK_008", "未登录或Token已失效");

    ErrorCode ADMIN_PERMISSION_REQUIRED =
            new ErrorCode(403, "FEEDBACK_009", "无权限访问后台反馈列表");

    // ==================== service层错误码 ====================

    ErrorCode FEEDBACK_NOT_EXIST =
            new ErrorCode(404, "FEEDBACK_010", "反馈记录不存在");

    ErrorCode FEEDBACK_TARGET_NOT_EXIST =
            new ErrorCode(400, "FEEDBACK_011", "反馈目标不存在");

    ErrorCode FEEDBACK_CREATE_FAILED =
            new ErrorCode(500, "FEEDBACK_012", "反馈提交失败");

    ErrorCode FEEDBACK_UPDATE_FAILED =
            new ErrorCode(500, "FEEDBACK_013", "反馈修改失败");

    ErrorCode FEEDBACK_DELETE_FAILED =
            new ErrorCode(500, "FEEDBACK_014", "反馈删除失败");

    ErrorCode FEEDBACK_PAGE_QUERY_FAILED =
            new ErrorCode(500, "FEEDBACK_015", "反馈分页查询失败");

    ErrorCode FEEDBACK_DETAIL_QUERY_FAILED =
            new ErrorCode(500, "FEEDBACK_016", "反馈详情查询失败");

    ErrorCode FEEDBACK_OPERATION_FORBIDDEN =
            new ErrorCode(403, "FEEDBACK_017", "无权限操作该反馈记录");

    ErrorCode WARNING_TARGET_ID_INVALID =
            new ErrorCode(400, "FEEDBACK_018", "预警反馈目标ID不合法");

    ErrorCode AI_TARGET_ID_INVALID =
            new ErrorCode(400, "FEEDBACK_019", "AI反馈目标ID不合法");

    ErrorCode AI_TARGET_MESSAGE_INVALID =
            new ErrorCode(400, "FEEDBACK_020", "AI反馈目标消息不合法");

    ErrorCode FEEDBACK_TARGET_DETAIL_QUERY_FAILED =
            new ErrorCode(500, "FEEDBACK_021", "反馈目标摘要查询失败");
}
