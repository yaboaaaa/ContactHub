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

    @GetMapping("/random-nickname")
    public ApiResult<String> getRandomNickname() {
        return ApiResult.success(userService.generateRandomNickname());
    }

    @PostMapping
    public ApiResult<Void> register(@RequestBody Map<String, String> body,
                                    @RequestParam(required = false) String captcha,
                                    HttpSession session) {
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");
        String nickname = body.get("nickname");

        if (username == null || !username.trim().matches("^[a-zA-Z][a-zA-Z0-9]{3,19}$")) {
            return ApiResult.error(400, "用户名必须以英文开头，仅含英文和数字，长度4-20位");
        }
        if (email != null && !email.trim().isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ApiResult.error(400, "邮箱格式不正确");
        }
        try {
            captchaService.validateCaptcha(session, captcha);
            userService.register(username, password, email, nickname);
            return ApiResult.success();
        } catch (BusinessException e) {
            return ApiResult.error(e.getCode(), e.getMessage());
        }
    }
}