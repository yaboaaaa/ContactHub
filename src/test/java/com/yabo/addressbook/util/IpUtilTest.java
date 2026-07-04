package com.yabo.addressbook.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IpUtilTest {

    @Test
    void getClientIP_shouldReturnXForwardedForFirstIp() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 5.6.7.8");
        assertThat(IpUtil.getClientIP(request)).isEqualTo("1.2.3.4");
    }

    @Test
    void getClientIP_shouldReturnXRealIP_whenNoForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("9.8.7.6");
        assertThat(IpUtil.getClientIP(request)).isEqualTo("9.8.7.6");
    }

    @Test
    void getClientIP_shouldReturnRemoteAddr_whenNoHeaders() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        assertThat(IpUtil.getClientIP(request)).isEqualTo("127.0.0.1");
    }

    @Test
    void getClientIP_shouldTrimSpaces() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("  1.2.3.4 , 5.6.7.8");
        assertThat(IpUtil.getClientIP(request)).isEqualTo("1.2.3.4");
    }
}
