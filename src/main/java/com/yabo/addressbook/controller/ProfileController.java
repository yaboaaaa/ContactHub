package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/update")
    public ApiResult<Void> updateProfile(@RequestParam(required = false) String username,
                                         @RequestParam(required = false) String email) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updateProfile(currentUsername, username, email);
        return ApiResult.success();
    }

    @PostMapping("/password")
    public ApiResult<Void> updatePassword(@RequestBody Map<String, String> body) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            throw new BusinessException("参数不完整");
        }
        userService.updatePassword(currentUsername, oldPassword, newPassword);
        return ApiResult.success();
    }
}
