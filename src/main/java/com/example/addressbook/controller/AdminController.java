package com.example.addressbook.controller;

import com.example.addressbook.dto.ApiResult;
import com.example.addressbook.entity.User;
import com.example.addressbook.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = adminService.listUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/data")
    @ResponseBody
    public ApiResult<List<User>> getUsers() {
        List<User> users = adminService.listUsers();
        return ApiResult.success(users);
    }

    @PostMapping("/users")
    @ResponseBody
    public ApiResult<Void> createUser(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam String email) {
        adminService.createUser(username, password, email);
        return ApiResult.success();
    }

    @PutMapping("/users/{id}")
    @ResponseBody
    public ApiResult<Void> updateUser(@PathVariable Long id,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) Boolean enabled) {
        adminService.updateUser(id, email, enabled);
        return ApiResult.success();
    }

    @DeleteMapping("/users/{id}")
    @ResponseBody
    public ApiResult<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResult.success();
    }
}