package com.zhku.agriwarningplatform.common.exception;

import com.zhku.agriwarningplatform.common.errorcode.ErrorCode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 0:04
 */
public class ServiceException extends RuntimeException{
    /**
     * 错误码
     */
    private Integer code;
    /**
     * 错误信息
     */
    private String message;

    public ServiceException() {
    }

    public ServiceException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
