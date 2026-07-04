package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员接口", description = "管理员专用接口，需要ADMIN角色权限")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users/data")
    @ResponseBody
    @Operation(summary = "获取用户列表", description = "管理员获取所有用户列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ApiResult<List<User>> getUsers() {
        List<User> users = adminService.listUsers();
        return ApiResult.success(users);
    }

    @PostMapping("/users")
    @ResponseBody
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @ApiResponse(responseCode = "200", description = "创建成功")
    public ApiResult<Void> createUser(@Parameter(description = "用户名") @RequestParam String username,
                                      @Parameter(description = "密码") @RequestParam String password,
                                      @Parameter(description = "邮箱") @RequestParam String email) {
        adminService.createUser(username, password, email);
        return ApiResult.success();
    }

    @PutMapping("/users/{id}")
    @ResponseBody
    @Operation(summary = "更新用户", description = "管理员更新用户信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    public ApiResult<Void> updateUser(@Parameter(description = "用户ID") @PathVariable Long id,
                                      @Parameter(description = "用户名") @RequestParam(required = false) String username,
                                      @Parameter(description = "昵称") @RequestParam(required = false) String nickname,
                                      @Parameter(description = "邮箱") @RequestParam(required = false) String email,
                                      @Parameter(description = "是否启用") @RequestParam(required = false) Boolean enabled) {
        adminService.updateUser(id, username, nickname, email, enabled);
        return ApiResult.success();
    }

    @DeleteMapping("/users/{id}")
    @ResponseBody
    @Operation(summary = "删除用户", description = "管理员删除指定用户")
    @ApiResponse(responseCode = "200", description = "删除成功")
    public ApiResult<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResult.success();
    }

    @PutMapping("/users/{id}/reset-password")
    @ResponseBody
    @Operation(summary = "重置用户密码", description = "管理员重置指定用户的密码")
    @ApiResponse(responseCode = "200", description = "重置成功")
    public ApiResult<Void> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id,
                                         @Parameter(description = "新密码") @RequestParam String password) {
        adminService.resetPassword(id, password);
        return ApiResult.success();
    }
}