package com.yabo.addressbook.service;

import com.yabo.addressbook.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    @Value("${captcha.enabled:false}")
    private boolean captchaEnabled;

    /**
     * Validate captcha code from session.
     * @param session the HTTP session
     * @param code the user input captcha code
     * @throws BusinessException if captcha is invalid
     */
    public void validateCaptcha(HttpSession session, String code) {
        if (!captchaEnabled) {
            return; // Captcha not required in non-prod environments
        }

        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null) {
            throw new BusinessException("验证码已过期，请刷新");
        }

        // Clear captcha after validation (one-time use)
        session.removeAttribute("captcha");

        if (code == null || !code.toLowerCase().equals(sessionCaptcha)) {
            throw new BusinessException("验证码错误");
        }
    }
}