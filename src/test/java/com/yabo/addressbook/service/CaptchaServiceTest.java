package com.yabo.addressbook.service;

import com.yabo.addressbook.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CaptchaServiceTest {

    private final CaptchaService captchaService = new CaptchaService();

    @Test
    void validateCaptcha_shouldPass_whenDisabled() {
        ReflectionTestUtils.setField(captchaService, "captchaEnabled", false);
        HttpSession session = mock(HttpSession.class);
        assertThatCode(() -> captchaService.validateCaptcha(session, null)).doesNotThrowAnyException();
    }

    @Test
    void validateCaptcha_shouldPass_whenCodeMatches() {
        ReflectionTestUtils.setField(captchaService, "captchaEnabled", true);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("captcha")).thenReturn("abc");
        assertThatCode(() -> captchaService.validateCaptcha(session, "abc")).doesNotThrowAnyException();
    }

    @Test
    void validateCaptcha_shouldThrow_whenCaseMismatch() {
        ReflectionTestUtils.setField(captchaService, "captchaEnabled", true);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("captcha")).thenReturn("ABC");
        assertThatThrownBy(() -> captchaService.validateCaptcha(session, "abc"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("验证码错误");
    }

    @Test
    void validateCaptcha_shouldThrow_whenCodeIncorrect() {
        ReflectionTestUtils.setField(captchaService, "captchaEnabled", true);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("captcha")).thenReturn("abc");
        assertThatThrownBy(() -> captchaService.validateCaptcha(session, "xyz"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("验证码错误");
    }

    @Test
    void validateCaptcha_shouldThrow_whenCodeNull() {
        ReflectionTestUtils.setField(captchaService, "captchaEnabled", true);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("captcha")).thenReturn("abc");
        assertThatThrownBy(() -> captchaService.validateCaptcha(session, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("验证码错误");
    }

    @Test
    void validateCaptcha_shouldThrow_whenSessionExpired() {
        ReflectionTestUtils.setField(captchaService, "captchaEnabled", true);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("captcha")).thenReturn(null);
        assertThatThrownBy(() -> captchaService.validateCaptcha(session, "abc"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("验证码已过期，请刷新");
    }

    @Test
    void validateCaptcha_shouldRemoveAfterValidation() {
        ReflectionTestUtils.setField(captchaService, "captchaEnabled", true);
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("captcha")).thenReturn("abc");
        captchaService.validateCaptcha(session, "abc");
        verify(session).removeAttribute("captcha");
    }
}
