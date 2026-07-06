package com.rent.managesystem.common.exception;

import com.rent.managesystem.common.ErrorCode;
import com.rent.managesystem.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * <p>使用 {@code @RestControllerAdvice} + {@code @ExceptionHandler} 实现
 * 统一捕获 Controller 层抛出的所有异常，避免将堆栈信息暴露给前端。
 *
 * <p><b>设计原则：</b>
 * <ul>
 *   <li>任何异常都不会返回堆栈跟踪给前端</li>
 *   <li>参数校验异常 → 返回具体字段级别的错误提示</li>
 *   <li>业务异常 → 返回预定义的错误码和消息</li>
 *   <li>未知异常 → 返回通用"系统繁忙"提示，同时打印详细日志到控制台</li>
 * </ul>
 *
 * @author 连梓祺 & 团队
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务自定义异常 ====================

    /**
     * 处理业务异常（由代码主动抛出的预期内异常）
     * 触发方式: throw new BizException(ErrorCode.USER_NOT_FOUND)
     */
    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        log.warn("业务异常: [{}] {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常 ====================

    /**
     * 处理 @RequestBody + @Valid 校验失败
     * 例如: POST /api/user 的 JSON body 字段不满足 @NotNull 等约束
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", errorMsg);
        return Result.error(ErrorCode.PARAM_INVALID.getCode(), errorMsg);
    }

    /**
     * 处理表单参数校验失败（@Validated + 表单提交场景）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String errorMsg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("表单参数绑定失败: {}", errorMsg);
        return Result.error(ErrorCode.PARAM_INVALID.getCode(), errorMsg);
    }

    /**
     * 处理缺少必填请求参数（@RequestParam 缺失）
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少必填参数: {}", e.getParameterName());
        return Result.error(ErrorCode.PARAM_MISSING.getCode(),
                "缺少必填参数: " + e.getParameterName());
    }

    /**
     * 处理参数类型转换错误（如 page=abc 这种情况）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {} 应为 {}",
                e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知类型");
        return Result.error(ErrorCode.PARAM_INVALID.getCode(),
                "参数[" + e.getName() + "]类型错误");
    }

    // ==================== 权限相关异常 ====================

    /**
     * 处理无权限访问（Spring Security 抛出）
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(ErrorCode.FORBIDDEN);
    }

    // ==================== HTTP请求异常 ====================

    /**
     * 处理不支持的HTTP方法（如对GET接口发了POST请求）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持的请求方法: {}", e.getMethod());
        return Result.error(ErrorCode.PARAM_INVALID.getCode(),
                "不支持的请求方式: " + e.getMethod());
    }

    /**
     * 处理文件大小超限
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("上传文件过大");
        return Result.error(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传文件大小超过限制");
    }

    // ==================== 兜底未知异常 ====================

    /**
     * 兜底处理所有未明确捕获的异常
     * 这是最后一道防线，防止500错误信息泄露给前端
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleUnknownException(Exception e) {
        // 打印完整堆栈到日志（方便排查），但只返回通用提示给前端
        log.error("未预期的异常", e);
        return Result.error(ErrorCode.SYSTEM_ERROR);
    }

}
