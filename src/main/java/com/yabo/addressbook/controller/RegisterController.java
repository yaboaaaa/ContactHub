package com.yabo.addressbook.controller;

import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.service.CaptchaService;
import com.yabo.addressbook.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    private final UserService userService;
    private final CaptchaService captchaService;

    public RegisterController(UserService userService, CaptchaService captchaService) {
        this.userService = userService;
        this.captchaService = captchaService;
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String captcha,
                           HttpSession session,
                           Model model) {
        try {
            captchaService.validateCaptcha(session, captcha);
            userService.register(username, password, email);
            return "redirect:/login?registered";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}