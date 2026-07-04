package com.yabo.addressbook.exception;

import com.yabo.addressbook.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessException_shouldReturnCodeAndMessage() {
        BusinessException ex = new BusinessException(400, "测试业务异常");
        ApiResult<?> result = handler.handleBusinessException(ex);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("测试业务异常");
    }

    @Test
    void handleBusinessException_shouldHandleDefaultCode() {
        BusinessException ex = new BusinessException("默认错误");
        ApiResult<?> result = handler.handleBusinessException(ex);
        assertThat(result.getMessage()).isEqualTo("默认错误");
    }

    @Test
    void handleAccessDeniedException_shouldReturn403() {
        AccessDeniedException ex = new AccessDeniedException("无权限");
        ApiResult<?> result = handler.handleAccessDeniedException(ex);
        assertThat(result.getCode()).isEqualTo(403);
        assertThat(result.getMessage()).isEqualTo("权限不足");
    }

    @Test
    void handleResponseStatusException_shouldReturnCode() {
        ResponseStatusException ex = new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "未找到");
        ApiResult<?> result = handler.handleResponseStatusException(ex);
        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getMessage()).isEqualTo("未找到");
    }

    @Test
    void handleResponseStatusException_shouldUseDefaultMessage() {
        ResponseStatusException ex = new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST);
        ApiResult<?> result = handler.handleResponseStatusException(ex);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("请求错误");
    }

    @Test
    void handleException_shouldReturn500() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");
        ApiResult<?> result = handler.handleException(new RuntimeException("测试异常"), request);
        assertThat(result.getCode()).isEqualTo(500);
        assertThat(result.getMessage()).contains("服务器内部错误");
    }
}
