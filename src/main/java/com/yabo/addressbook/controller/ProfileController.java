package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/user/profile")
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;

    public ProfileController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showProfile(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PutMapping("/update")
    @ResponseBody
    public ApiResult<Void> updateProfile(@RequestParam(required = false) String username,
                                         @RequestParam(required = false) String email) {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            userService.updateProfile(currentUsername, username, email);
            return ApiResult.success();
        } catch (BusinessException e) {
            return ApiResult.error(400, e.getMessage());
        }
    }

    @PostMapping("/password")
    @ResponseBody
    public ApiResult<Void> updatePassword(@RequestBody Map<String, String> body) {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");
            if (oldPassword == null || newPassword == null) {
                return ApiResult.error(400, "参数不完整");
            }
            userService.updatePassword(currentUsername, oldPassword, newPassword);
            return ApiResult.success();
        } catch (BusinessException e) {
            return ApiResult.error(400, e.getMessage());
        }
    }
}
