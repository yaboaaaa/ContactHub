package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.GroupService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    public GroupController(GroupService groupService, UserRepository userRepository) {
        this.groupService = groupService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResult<?> listGroups() {
        Long userId = getCurrentUserId();
        return ApiResult.success(groupService.listGroups(userId));
    }

    @PostMapping
    public ApiResult<?> createGroup(@RequestParam String name) {
        Long userId = getCurrentUserId();
        groupService.createGroup(userId, name);
        return ApiResult.success();
    }

    @PutMapping("/{id}")
    public ApiResult<?> updateGroup(@PathVariable Long id, @RequestParam String name) {
        Long userId = getCurrentUserId();
        groupService.updateGroup(id, userId, name);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    public ApiResult<?> deleteGroup(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        groupService.deleteGroup(id, userId);
        return ApiResult.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}