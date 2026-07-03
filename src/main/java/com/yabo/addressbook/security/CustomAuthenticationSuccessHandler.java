package com.yabo.addressbook.security;

import com.yabo.addressbook.service.LoginAttemptService;
import com.yabo.addressbook.util.IpUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;

    public CustomAuthenticationSuccessHandler(LoginAttemptService loginAttemptService) {
        super("/contacts");
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        String ip = IpUtil.getClientIP(request);
        loginAttemptService.loginSucceeded(ip);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}