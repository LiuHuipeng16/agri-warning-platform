package com.zhku.agriwarningplatform.common.handler;

import com.zhku.agriwarningplatform.common.errorcode.GlobalErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 12290
 * Date: 2026-04-09
 * Time: 10:23
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 统一处理controller层异常
     * @param e
     * @return
     */
    @ExceptionHandler(ControllerException.class)
    public CommonResult<?> handlerServiceException(ControllerException e){
        log.error("ControllerException:", e);
        return CommonResult.error(e.getCode(),e.getMessage());
    }

    /**
     * 统一处理service层异常
     * @param e
     * @return
     */
    @ExceptionHandler(ServiceException.class)
    public CommonResult<?> handlerServiceException(ServiceException e){
        log.error("ServiceException:", e);
        return CommonResult.error(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<?> handlerServiceException(Exception e){
        log.error("服务异常:", e);
        return CommonResult.error(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),e.getMessage());
    }
}
