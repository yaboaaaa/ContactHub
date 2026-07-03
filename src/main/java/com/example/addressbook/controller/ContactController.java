package com.example.addressbook.controller;

import com.example.addressbook.dto.ApiResult;
import com.example.addressbook.dto.ContactDTO;
import com.example.addressbook.dto.ImportResult;
import com.example.addressbook.dto.PageDTO;
import com.example.addressbook.entity.User;
import com.example.addressbook.repository.ContactGroupRepository;
import com.example.addressbook.repository.ContactRepository;
import com.example.addressbook.repository.UserRepository;
import com.example.addressbook.service.ContactService;
import com.example.addressbook.service.GroupService;
import com.example.addressbook.util.ExcelUtil;
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
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/contacts")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;
    private final GroupService groupService;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final ContactGroupRepository contactGroupRepository;

    public ContactController(ContactService contactService,
                             GroupService groupService,
                             UserRepository userRepository,
                             ContactRepository contactRepository,
                             ContactGroupRepository contactGroupRepository) {
        this.contactService = contactService;
        this.groupService = groupService;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.contactGroupRepository = contactGroupRepository;
    }

    @GetMapping
    public String contactsPage(Model model) {
        Long userId = getCurrentUserId();
        model.addAttribute("groups", groupService.listGroups(userId));
        return "contacts";
    }

    @GetMapping("/data")
    @ResponseBody
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
            map.put("name", c.getName());
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
            if (c.getGroup() != null) {
                var groupMap = new java.util.HashMap<String, Object>();
                groupMap.put("id", c.getGroup().getId());
                groupMap.put("name", c.getGroup().getName());
                map.put("group", groupMap);
            }
            return map;
        }).collect(java.util.stream.Collectors.toList());

        var pageDTO = new com.example.addressbook.dto.PageDTO<>();
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
    public ApiResult<PageDTO<?>> getRecycleData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        var pageResult = contactService.getRecycleBin(userId, page, size);

        @SuppressWarnings("unchecked")
        var dtoContent = (java.util.List<Object>) (java.util.List<?>) pageResult.getContent().stream().map(c -> {
            var map = new java.util.HashMap<String, Object>();
            map.put("id", c.getId());
            map.put("name", c.getName());
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

        var pageDTO = new com.example.addressbook.dto.PageDTO<>();
        pageDTO.setContent(dtoContent);
        pageDTO.setTotalPages(pageResult.getTotalPages());
        pageDTO.setTotalElements(pageResult.getTotalElements());
        pageDTO.setCurrentPage(pageResult.getNumber() + 1);
        pageDTO.setSize(pageResult.getSize());
        return ApiResult.success(pageDTO);
    }

    @PutMapping("/{id}/restore")
    @ResponseBody
    public ApiResult<Void> restoreContact(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        contactService.restore(id, userId);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}/permanent")
    @ResponseBody
    public ApiResult<Void> permanentDeleteContact(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        contactService.permanentDelete(id, userId);
        return ApiResult.success();
    }

    @DeleteMapping("/recycle")
    @ResponseBody
    public ApiResult<Void> emptyRecycleBin() {
        Long userId = getCurrentUserId();
        contactService.emptyRecycleBin(userId);
        return ApiResult.success();
    }

    @PostMapping
    @ResponseBody
    public ApiResult<Void> createContact(@Valid @RequestBody ContactDTO contactDTO) {
        Long userId = getCurrentUserId();
        contactService.createContact(contactDTO, userId);
        return ApiResult.success();
    }

    @GetMapping("/export")
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
        // Use a large page size to get all matching contacts
        var contacts = contactService.searchContacts(
                userId, keyword, phone, province, city, district,
                company, jobTitle, groupId, 0, 10000).getContent();
        ExcelUtil.exportContacts(contacts, response);
    }

    @PostMapping("/import")
    @ResponseBody
    public ApiResult<ImportResult> importContacts(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        ImportResult importResult = ExcelUtil.importContacts(
                file, userId, userRepository, contactGroupRepository, contactRepository);
        return ApiResult.success(importResult);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ApiResult<Void> updateContact(@PathVariable Long id,
                                         @Valid @RequestBody ContactDTO contactDTO) {
        Long userId = getCurrentUserId();
        contactService.updateContact(id, contactDTO, userId);
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ApiResult<Void> deleteContact(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        contactService.softDelete(id, userId);
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