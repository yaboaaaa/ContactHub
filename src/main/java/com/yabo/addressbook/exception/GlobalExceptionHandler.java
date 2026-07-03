package com.yabo.addressbook.exception;

import com.yabo.addressbook.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

    @ExceptionHandler(BusinessException.class)
    public @ResponseBody Object handleBusinessException(BusinessException e, HttpServletRequest request) {
        if (isAjaxRequest(request)) {
            return ApiResult.error(e.getCode(), e.getMessage());
        }
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("code", e.getCode());
        mav.addObject("message", e.getMessage());
        return mav;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @ResponseBody Object handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append("; ");
        }
        String message = sb.toString();
        if (isAjaxRequest(request)) {
            return ApiResult.error(400, message);
        }
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("code", 400);
        mav.addObject("message", message);
        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public @ResponseBody Object handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        if (isAjaxRequest(request)) {
            return ApiResult.error(403, "权限不足");
        }
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("code", 403);
        mav.addObject("message", "权限不足");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public @ResponseBody Object handleException(Exception e, HttpServletRequest request) {
        if (isAjaxRequest(request)) {
            return ApiResult.error(500, "服务器内部错误");
        }
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("code", 500);
        mav.addObject("message", "服务器内部错误");
        return mav;
    }
}