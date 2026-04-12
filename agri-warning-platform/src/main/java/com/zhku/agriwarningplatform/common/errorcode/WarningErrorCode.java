package com.zhku.agriwarningplatform.common.errorcode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-10
 * Time: 23:03
 */
public interface WarningErrorCode {
    //controller层错误码
    ErrorCode WARNING_NOT_EXIST =
            new ErrorCode(404, "WARNING_001", "预警不存在");

    ErrorCode WARNING_TITLE_EMPTY =
            new ErrorCode(400, "WARNING_002", "预警标题不能为空");

    ErrorCode WARNING_CONTENT_EMPTY =
            new ErrorCode(400, "WARNING_003", "预警内容不能为空");



    //service层错误码
}
