package com.yabo.addressbook.security;

import com.yabo.addressbook.service.CaptchaService;
import com.yabo.addressbook.service.LoginAttemptService;
import com.yabo.addressbook.util.IpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Component
public class IpLockFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;
    private final MessageSource messageSource;

    public IpLockFilter(LoginAttemptService loginAttemptService,
                        CaptchaService captchaService,
                        MessageSource messageSource) {
        this.loginAttemptService = loginAttemptService;
        this.captchaService = captchaService;
        this.messageSource = messageSource;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Only check login POST requests
        if ("POST".equalsIgnoreCase(request.getMethod())
                && "/login".equals(request.getServletPath())) {

            Locale locale = request.getLocale();

            // 1. Check IP lock
            String ip = IpUtil.getClientIP(request);
            if (loginAttemptService.isLocked(ip)) {
                long remainingSec = loginAttemptService.getLockTimeRemainingSeconds(ip);
                long minutes = remainingSec / 60;
                long seconds = remainingSec % 60;
                String errorMsg = (minutes > 0)
                    ? messageSource.getMessage("login.error.locked", new Object[]{minutes, seconds}, locale)
                    : messageSource.getMessage("login.error.lockedShort", new Object[]{seconds}, locale);
                request.getSession().setAttribute("loginErrorMsg", errorMsg);
                response.sendRedirect("/login?error");
                return;
            }

            // 2. Check captcha (only when captcha is enabled)
            String captchaCode = request.getParameter("captcha");
            try {
                captchaService.validateCaptcha(request.getSession(), captchaCode);
            } catch (Exception e) {
                String errorMsg = messageSource.getMessage("login.error.captcha", null, "验证码错误", locale);
                request.getSession().setAttribute("loginErrorMsg", errorMsg);
                response.sendRedirect("/login?error");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}