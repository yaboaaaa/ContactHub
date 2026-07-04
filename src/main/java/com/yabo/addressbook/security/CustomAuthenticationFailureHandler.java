package com.yabo.addressbook.security;

import com.yabo.addressbook.service.LoginAttemptService;
import com.yabo.addressbook.util.IpUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;
    private final MessageSource messageSource;

    public CustomAuthenticationFailureHandler(LoginAttemptService loginAttemptService,
                                               MessageSource messageSource) {
        super("/login.html?error");
        this.loginAttemptService = loginAttemptService;
        this.messageSource = messageSource;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String ip = IpUtil.getClientIP(request);
        loginAttemptService.loginFailed(ip);

        Locale locale = request.getLocale();

        String errorMsg;
        if (loginAttemptService.isLocked(ip)) {
            long remainingSec = loginAttemptService.getLockTimeRemainingSeconds(ip);
            long minutes = remainingSec / 60;
            long seconds = remainingSec % 60;
            if (minutes > 0) {
                errorMsg = messageSource.getMessage("login.error.locked", new Object[]{minutes, seconds}, locale);
            } else {
                errorMsg = messageSource.getMessage("login.error.lockedShort", new Object[]{seconds}, locale);
            }
        } else {
            int remaining = loginAttemptService.getRemainingAttempts(ip);
            errorMsg = messageSource.getMessage("login.error.remaining", new Object[]{remaining}, locale);
        }

        String encodedMsg = URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
        response.sendRedirect("/login.html?error&msg=" + encodedMsg);
    }
}