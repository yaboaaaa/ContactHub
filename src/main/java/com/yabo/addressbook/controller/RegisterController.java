package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.service.CaptchaService;
import com.yabo.addressbook.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/register")
public class RegisterController {

    private final UserService userService;
    private final CaptchaService captchaService;

    public RegisterController(UserService userService, CaptchaService captchaService) {
        this.userService = userService;
        this.captchaService = captchaService;
    }

    @GetMapping("/check-username")
    public ApiResult<Boolean> checkUsername(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return ApiResult.success(false);
        }
        return ApiResult.success(!userService.existsByUsername(username.trim()));
    }

    @PostMapping
    public ApiResult<Void> register(@RequestBody Map<String, String> body,
                                    @RequestParam(required = false) String captcha,
                                    HttpSession session) {
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");
        try {
            captchaService.validateCaptcha(session, captcha);
            userService.register(username, password, email);
            return ApiResult.success();
        } catch (BusinessException e) {
            return ApiResult.error(e.getCode(), e.getMessage());
        }
    }
}