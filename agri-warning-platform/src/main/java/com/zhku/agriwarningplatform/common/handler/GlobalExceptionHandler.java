package com.zhku.agriwarningplatform.common.handler;

import com.zhku.agriwarningplatform.common.errorcode.GlobalErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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
     * 统一处理 controller 层异常
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(ControllerException.class)
    public CommonResult<?> handleControllerException(ControllerException e) {
        log.error("ControllerException:{},{},{}", e.getCode(), e.getInternalCode(), e.getMessage(), e);
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 统一处理 service 层异常
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(ServiceException.class)
    public CommonResult<?> handleServiceException(ServiceException e) {
        log.error("ServiceException:{},{},{}", e.getCode(), e.getInternalCode(), e.getMessage(), e);
        return CommonResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 统一处理未预知异常
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<?> handleException(Exception e) {
        log.error("系统未捕获异常:", e);
        return CommonResult.error(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
    }

    /**
     * 处理表单参数 / Query 参数校验异常
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(BindException.class)
    public CommonResult<?> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";

        log.warn("BindException: {}", message, e);
        return CommonResult.error(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理 @RequestParam / @PathVariable 参数校验异常
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<?> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("参数校验失败");

        log.warn("ConstraintViolationException: {}", message, e);
        return CommonResult.error(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理方法级参数校验异常
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public CommonResult<?> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        String message = e.getAllValidationResults()
                .stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("参数校验失败");

        log.warn("HandlerMethodValidationException: {}", message, e);
        return CommonResult.error(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public CommonResult<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {

        String paramName = e.getName();
        String targetType = e.getRequiredType() != null ?
                e.getRequiredType().getSimpleName() : "unknown";

        log.warn("MethodArgumentTypeMismatchException: 参数名={}, 参数值={}, 目标类型={}",
                paramName,
                e.getValue(),
                targetType,
                e);

        return CommonResult.error(
                GlobalErrorCode.BAD_REQUEST.getCode(),
                "请求参数 " + paramName + " 类型错误"
        );
    }

    /**
     * 处理请求体不可读异常，例如 JSON 格式错误
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("HttpMessageNotReadableException: 请求JSON格式错误", e);
        return CommonResult.error(GlobalErrorCode.BAD_REQUEST.getCode(), "请求JSON格式错误");
    }

    /**
     * 处理方法参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();

        if (fieldError == null) {
            log.warn("MethodArgumentNotValidException: 无字段错误信息", e);
            return CommonResult.error(
                    GlobalErrorCode.BAD_REQUEST.getCode(),
                    "请求参数错误"
            );
        }

        String fieldName = fieldError.getField();
        Object rejectedValue = fieldError.getRejectedValue();
        String defaultMessage = fieldError.getDefaultMessage();

        log.warn("MethodArgumentNotValidException: 参数名={}, 参数值={}, 默认消息={}",
                fieldName, rejectedValue, defaultMessage, e);

        String message;
        if (defaultMessage != null && defaultMessage.contains("Failed to convert")) {
            message = "请求参数 " + fieldName + " 类型错误";
        } else {
            message = defaultMessage;
        }

        return CommonResult.error(
                GlobalErrorCode.BAD_REQUEST.getCode(),
                message
        );
    }
}