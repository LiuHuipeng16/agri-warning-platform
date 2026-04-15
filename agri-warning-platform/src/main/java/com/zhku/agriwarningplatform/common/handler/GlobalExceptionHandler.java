package com.zhku.agriwarningplatform.common.handler;

import com.zhku.agriwarningplatform.common.errorcode.GlobalErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolation;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;

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
        log.error("ControllerException:{},{},{}",e.getCode(),e.getInternalCode(),e.getMessage(), e);
        return CommonResult.error(e.getCode(),e.getMessage());
    }

    /**
     * 统一处理service层异常
     * @param e
     * @return
     */
    @ExceptionHandler(ServiceException.class)
    public CommonResult<?> handlerServiceException(ServiceException e){
        log.error("ServiceException:{},{},{}",e.getCode(),e.getInternalCode(),e.getMessage(), e);
        return CommonResult.error(e.getCode(),e.getMessage());
    }

    /**
     * 统一处理未预知异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<?> handlerServiceException(Exception e){
        log.error("服务异常:", e);
        return CommonResult.error(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),e.getMessage());
    }

    /**
     * 处理 @RequestBody 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.warn("参数校验异常: {}", message);
        return CommonResult.error(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理表单参数 / Query 参数校验异常
     */
    @ExceptionHandler(BindException.class)
    public CommonResult<?> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";

        log.warn("参数绑定异常: {}", message);
        return CommonResult.error(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理 @RequestParam / @PathVariable 校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("参数校验失败");

        log.warn("参数约束异常: {}", message);
        return CommonResult.error(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }


    @ExceptionHandler(HandlerMethodValidationException.class)
    public CommonResult<?> handleHandlerMethodValidationException(HandlerMethodValidationException e) {

        String message = e.getAllValidationResults()
                .stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("参数校验失败");

        log.warn("参数校验异常: {}", message);
        return CommonResult.error(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public CommonResult<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {

        return CommonResult.error(
                400,
                "参数类型错误: " + e.getName()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return CommonResult.error(400, "请求JSON格式错误");
    }

}
