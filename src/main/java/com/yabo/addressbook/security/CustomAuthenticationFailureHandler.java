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

        // Build the appropriate error message
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

        // Store error message in session for the login page to display
        request.getSession().setAttribute("loginErrorMsg", errorMsg);

        super.onAuthenticationFailure(request, response, exception);
    }
}