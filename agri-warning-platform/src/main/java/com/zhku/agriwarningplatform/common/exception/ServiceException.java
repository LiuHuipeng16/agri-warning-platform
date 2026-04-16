package com.zhku.agriwarningplatform.common.exception;

import com.zhku.agriwarningplatform.common.errorcode.ErrorCode;
import lombok.Getter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 0:04
 */
@Getter
public class ServiceException extends RuntimeException{
    /**
     * 错误码
     */
    private Integer code;
    /**
     * 内部错误码
     */
    private String internalCode;
    /**
     * 错误信息
     */
    private String message;

    public ServiceException() {
    }

    public ServiceException(String message, Integer code, String internalCode, String message1) {
        super(message);
        this.code = code;
        this.internalCode = internalCode;
        this.message = message1;
    }
    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.internalCode = errorCode.getInternalCode();
        this.message = errorCode.getMessage();
    }
}
