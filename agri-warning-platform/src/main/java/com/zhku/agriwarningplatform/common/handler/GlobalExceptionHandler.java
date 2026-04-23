package com.zhku.agriwarningplatform.common.handler;

import com.zhku.agriwarningplatform.common.errorcode.GlobalErrorCode;
import com.zhku.agriwarningplatform.common.exception.ControllerException;
import com.zhku.agriwarningplatform.common.exception.ServiceException;
import com.zhku.agriwarningplatform.common.result.CommonResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @ExceptionHandler(NoResourceFoundException.class)
    public CommonResult<Void> handleNoResource(NoResourceFoundException e) {
        log.error("接口不存在:", e);
        return CommonResult.error(404, "接口不存在");
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
    public CommonResult<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("JSON解析异常", e);
        return CommonResult.error(400, "请求JSON格式错误：" + e.getMostSpecificCause().getMessage());
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

    /**
     * 处理数据库字段约束异常，例如 字段为空,字段截断
     *
     * @param e 异常
     * @return 响应结果
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public CommonResult<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据库字段约束异常:", e);

        String message = extractDataIntegrityViolationMessage(e);
        return CommonResult.error(GlobalErrorCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 解析异常信息
     * @param e
     * @return
     */
    private String extractDataIntegrityViolationMessage(DataIntegrityViolationException e) {
        Throwable rootCause = getRootCause(e);
        String errorMessage = rootCause != null ? rootCause.getMessage() : e.getMessage();

        if (errorMessage == null || errorMessage.isEmpty()) {
            return "参数不合法";
        }

        if (errorMessage.contains("Data too long for column")) {
            String columnName = extractColumnName(errorMessage);
            if (columnName != null) {
                return convertColumnNameToFieldMessage(columnName);
            }
            return "字段长度超出限制";
        }

        if (errorMessage.contains("Duplicate entry")) {
            return "数据冲突，请勿重复提交";
        }

        if (errorMessage.contains("cannot be null")) {
            String columnName = extractColumnNameForNotNull(errorMessage);
            if (columnName != null) {
                return convertColumnNameToNotNullMessage(columnName);
            }
            return "必填字段不能为空";
        }

        return "参数不合法";
    }

    /**
     * 根异常提取
     * @param throwable
     * @return
     */
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * 提取字段名
     * @param errorMessage
     * @return
     */
    private String extractColumnName(String errorMessage) {
        Pattern pattern = Pattern.compile("Data too long for column '([^']+)'");
        Matcher matcher = pattern.matcher(errorMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 解析非空约束
     * @param errorMessage
     * @return
     */
    private String extractColumnNameForNotNull(String errorMessage) {
        Pattern pattern = Pattern.compile("Column '([^']+)' cannot be null");
        Matcher matcher = pattern.matcher(errorMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 字段映射
     * @param columnName
     * @return
     */
    private String convertColumnNameToFieldMessage(String columnName) {
        if ("chat_id".equals(columnName)) {
            return "chatId长度不能超过64";
        }
        if ("title".equals(columnName)) {
            return "标题长度超出限制";
        }
        if ("context_type".equals(columnName)) {
            return "contextType长度超出限制";
        }
        return columnName + "字段长度超出限制";
    }

    /**
     * 处理非空约束
     * @param columnName
     * @return
     */
    private String convertColumnNameToNotNullMessage(String columnName) {
        if ("chat_id".equals(columnName)) {
            return "chatId不能为空";
        }
        if ("user_id".equals(columnName)) {
            return "userId不能为空";
        }
        return columnName + "不能为空";
    }
}