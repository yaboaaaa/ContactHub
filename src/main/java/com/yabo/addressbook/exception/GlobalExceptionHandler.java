package com.yabo.addressbook.exception;

import com.yabo.addressbook.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ApiResult<?> handleBusinessException(BusinessException e) {
        return ApiResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ");
        }
        return ApiResult.error(400, sb.toString());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResult<?> handleAccessDeniedException(AccessDeniedException e) {
        return ApiResult.error(403, "权限不足");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResult<?> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("Static resource not found: {}", request.getRequestURI());
        return ApiResult.error(404, "资源不存在");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ApiResult<?> handleResponseStatusException(ResponseStatusException e) {
        int code = e.getStatusCode().value();
        String message = e.getReason() != null ? e.getReason() : "请求错误";
        return ApiResult.error(code, message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), e);
        return ApiResult.error(500, "服务器内部错误: " + e.getMessage());
    }
}