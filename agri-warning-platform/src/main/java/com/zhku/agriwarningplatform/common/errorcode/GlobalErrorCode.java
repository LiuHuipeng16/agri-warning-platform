package com.zhku.agriwarningplatform.common.errorcode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 10:48
 */
public interface GlobalErrorCode {
    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, "GLOBAL","系统异常！");
}
