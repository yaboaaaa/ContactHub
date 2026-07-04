package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.dto.GroupRequest;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups")
@Tag(name = "分组管理", description = "联系人分组的增删改查操作")
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    public GroupController(GroupService groupService, UserRepository userRepository) {
        this.groupService = groupService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "获取分组列表", description = "获取当前用户的所有分组")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ApiResult<?> listGroups() {
        Long userId = getCurrentUserId();
        return ApiResult.success(groupService.listGroups(userId));
    }

    @GetMapping("/check-name")
    @Operation(summary = "检查分组名是否重复", description = "检查指定分组名称在当前用户下是否已存在")
    public ApiResult<?> checkName(@RequestParam String name, @RequestParam(required = false) Long excludeId) {
        Long userId = getCurrentUserId();
        boolean exists = groupService.existsByName(userId, name, excludeId);
        return ApiResult.success(java.util.Map.of("exists", exists));
    }

    @PostMapping
    @Operation(summary = "创建分组", description = "为当前用户创建一个新的联系人分组")
    @ApiResponse(responseCode = "200", description = "创建成功")
    public ApiResult<?> createGroup(@Valid @RequestBody GroupRequest request) {
        Long userId = getCurrentUserId();
        groupService.createGroup(userId, request.getName());
        return ApiResult.success();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分组", description = "更新指定分组的名称")
    @ApiResponse(responseCode = "200", description = "更新成功")
    public ApiResult<?> updateGroup(@Parameter(description = "分组ID") @PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        Long userId = getCurrentUserId();
        groupService.updateGroup(id, userId, request.getName());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分组", description = "删除指定的联系人分组")
    @ApiResponse(responseCode = "200", description = "删除成功")
    public ApiResult<?> deleteGroup(@Parameter(description = "分组ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        groupService.deleteGroup(id, userId);
        return ApiResult.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
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