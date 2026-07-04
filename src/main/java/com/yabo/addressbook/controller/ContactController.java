package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.dto.ContactDTO;
import com.yabo.addressbook.dto.PageDTO;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.ContactService;
import com.yabo.addressbook.service.GroupService;
import com.yabo.addressbook.util.ExcelUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/contacts")
@Tag(name = "联系人管理", description = "联系人的增删改查及导入导出操作")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;
    private final GroupService groupService;
    private final UserRepository userRepository;

    public ContactController(ContactService contactService,
                             GroupService groupService,
                             UserRepository userRepository) {
        this.contactService = contactService;
        this.groupService = groupService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String contactsPage(Model model) {
        Long userId = getCurrentUserId();
        model.addAttribute("groups", groupService.listGroups(userId));
        return "contacts";
    }

    @GetMapping("/data")
    @ResponseBody
    @Operation(summary = "获取联系人列表", description = "支持按关键词、电话、地区、公司等多条件分页搜索联系人")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ApiResult<PageDTO<?>> getContactsData(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        // Force initialization of lazy properties within the service transaction
        var pageResult = contactService.searchContacts(
                userId, keyword, phone, province, city, district,
                company, jobTitle, groupId, page, size);

        // Convert entities to plain Maps to avoid lazy loading during JSON serialization
        @SuppressWarnings("unchecked")
        var dtoContent = (java.util.List<Object>) (java.util.List<?>) pageResult.getContent().stream().map(c -> {
            var map = new java.util.HashMap<String, Object>();
            map.put("id", c.getId());
            map.put("uid", c.getUid());
            map.put("name", c.getName());
            map.put("displayName", c.getDisplayName());
            map.put("familyName", c.getFamilyName());
            map.put("givenName", c.getGivenName());
            map.put("gender", c.getGender());
            map.put("phoneMobile", c.getPhoneMobile());
            map.put("phoneHome", c.getPhoneHome());
            map.put("phoneWork", c.getPhoneWork());
            map.put("email", c.getEmail());
            map.put("company", c.getCompany());
            map.put("jobTitle", c.getJobTitle());
            map.put("province", c.getProvince());
            map.put("city", c.getCity());
            map.put("district", c.getDistrict());
            map.put("addressDetail", c.getAddressDetail());
            map.put("birthday", c.getBirthday() != null ? c.getBirthday().toString() : null);
            map.put("notes", c.getNotes());
            map.put("avatarUrl", c.getAvatarUrl());
            if (c.getGroup() != null) {
                var groupMap = new java.util.HashMap<String, Object>();
                groupMap.put("id", c.getGroup().getId());
                groupMap.put("name", c.getGroup().getName());
                map.put("group", groupMap);
            }
            return map;
        }).collect(java.util.stream.Collectors.toList());

        var pageDTO = new com.yabo.addressbook.dto.PageDTO<>();
        pageDTO.setContent(dtoContent);
        pageDTO.setTotalPages(pageResult.getTotalPages());
        pageDTO.setTotalElements(pageResult.getTotalElements());
        pageDTO.setCurrentPage(pageResult.getNumber() + 1);
        pageDTO.setSize(pageResult.getSize());
        return ApiResult.success(pageDTO);
    }

    @GetMapping("/recycle")
    public String recyclePage() {
        return "recycle";
    }

    @GetMapping("/recycle/data")
    @ResponseBody
    @Operation(summary = "获取回收站列表", description = "分页获取已软删除的联系人")
    @ApiResponse(responseCode = "200", description = "获取成功")
    public ApiResult<PageDTO<?>> getRecycleData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        var pageResult = contactService.getRecycleBin(userId, page, size);

        @SuppressWarnings("unchecked")
        var dtoContent = (java.util.List<Object>) (java.util.List<?>) pageResult.getContent().stream().map(c -> {
            var map = new java.util.HashMap<String, Object>();
            map.put("id", c.getId());
            map.put("uid", c.getUid());
            map.put("name", c.getName());
            map.put("displayName", c.getDisplayName());
            map.put("familyName", c.getFamilyName());
            map.put("givenName", c.getGivenName());
            map.put("gender", c.getGender());
            map.put("phoneMobile", c.getPhoneMobile());
            map.put("phoneHome", c.getPhoneHome());
            map.put("phoneWork", c.getPhoneWork());
            map.put("email", c.getEmail());
            map.put("company", c.getCompany());
            map.put("jobTitle", c.getJobTitle());
            map.put("province", c.getProvince());
            map.put("city", c.getCity());
            map.put("district", c.getDistrict());
            map.put("addressDetail", c.getAddressDetail());
            map.put("birthday", c.getBirthday() != null ? c.getBirthday().toString() : null);
            map.put("notes", c.getNotes());
            map.put("deletedAt", c.getDeletedAt());
            if (c.getGroup() != null) {
                var groupMap = new java.util.HashMap<String, Object>();
                groupMap.put("id", c.getGroup().getId());
                groupMap.put("name", c.getGroup().getName());
                map.put("group", groupMap);
            }
            return map;
        }).collect(java.util.stream.Collectors.toList());

        var pageDTO = new com.yabo.addressbook.dto.PageDTO<>();
        pageDTO.setContent(dtoContent);
        pageDTO.setTotalPages(pageResult.getTotalPages());
        pageDTO.setTotalElements(pageResult.getTotalElements());
        pageDTO.setCurrentPage(pageResult.getNumber() + 1);
        pageDTO.setSize(pageResult.getSize());
        return ApiResult.success(pageDTO);
    }

    @PutMapping("/{id}/restore")
    @ResponseBody
    @Operation(summary = "恢复联系人", description = "从回收站恢复指定联系人")
    @ApiResponse(responseCode = "200", description = "恢复成功")
    public ApiResult<Void> restoreContact(@Parameter(description = "联系人ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        contactService.restore(id, userId);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}/permanent")
    @ResponseBody
    @Operation(summary = "永久删除联系人", description = "彻底删除指定联系人（不可恢复）")
    @ApiResponse(responseCode = "200", description = "删除成功")
    public ApiResult<Void> permanentDeleteContact(@Parameter(description = "联系人ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        contactService.permanentDelete(id, userId);
        return ApiResult.success();
    }

    @DeleteMapping("/recycle")
    @ResponseBody
    @Operation(summary = "清空回收站", description = "永久删除回收站中所有联系人的数据")
    @ApiResponse(responseCode = "200", description = "清空成功")
    public ApiResult<Void> emptyRecycleBin() {
        Long userId = getCurrentUserId();
        contactService.emptyRecycleBin(userId);
        return ApiResult.success();
    }

    @PostMapping
    @ResponseBody
    @Operation(summary = "创建联系人", description = "新增一个联系人")
    @ApiResponse(responseCode = "200", description = "创建成功")
    public ApiResult<Void> createContact(@Valid @RequestBody ContactDTO contactDTO) {
        Long userId = getCurrentUserId();
        contactService.createContact(contactDTO, userId);
        return ApiResult.success();
    }

    @GetMapping("/export")
    @Operation(summary = "导出联系人", description = "导出筛选后的联系人为Excel文件")
    public void exportContacts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) Long groupId,
            HttpServletResponse response) {
        Long userId = getCurrentUserId();
        var contacts = contactService.searchContacts(
                userId, keyword, phone, province, city, district,
                company, jobTitle, groupId, 0, 10000).getContent();
        ExcelUtil.exportContacts(contacts, response);
    }

    @GetMapping("/export/all")
    @Operation(summary = "导出全部联系人", description = "导出当前用户所有联系人为Excel文件")
    @ApiResponse(responseCode = "200", description = "导出成功")
    public void exportAllContacts(HttpServletResponse response) {
        Long userId = getCurrentUserId();
        var contacts = contactService.getAllContacts(userId);
        ExcelUtil.exportContacts(contacts, response);
    }

    @PutMapping("/{id}")
    @ResponseBody
    @Operation(summary = "更新联系人", description = "更新指定联系人的信息")
    @ApiResponse(responseCode = "200", description = "更新成功")
    public ApiResult<Void> updateContact(@Parameter(description = "联系人ID") @PathVariable Long id,
                                         @Valid @RequestBody ContactDTO contactDTO) {
        Long userId = getCurrentUserId();
        contactService.updateContact(id, contactDTO, userId);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @Operation(summary = "删除联系人", description = "将联系人移至回收站（软删除）")
    @ApiResponse(responseCode = "200", description = "删除成功")
    public ApiResult<Void> deleteContact(@Parameter(description = "联系人ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        contactService.softDelete(id, userId);
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