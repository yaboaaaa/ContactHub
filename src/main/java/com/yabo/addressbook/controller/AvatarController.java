package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.AvatarService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/user")
public class AvatarController {

    private final AvatarService avatarService;
    private final UserRepository userRepository;

    public AvatarController(AvatarService avatarService, UserRepository userRepository) {
        this.avatarService = avatarService;
        this.userRepository = userRepository;
    }

    /**
     * Get avatar URL for the currently authenticated user.
     * Returns JSON with the URL, or null if no custom avatar.
     */
    @GetMapping("/avatar")
    public ResponseEntity<ApiResult<Map<String, Object>>> getCurrentUserAvatar() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        String url = (user != null) ? user.getAvatarUrl() : null;
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        return ResponseEntity.ok(ApiResult.success(result));
    }

    /**
     * Get avatar URL for a specific user by user id.
     */
    @GetMapping("/{id}/avatar")
    public ResponseEntity<ApiResult<Map<String, Object>>> getUserAvatarById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        String url = (user != null) ? user.getAvatarUrl() : null;
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        return ResponseEntity.ok(ApiResult.success(result));
    }

    /**
     * Upload avatar for the currently authenticated user.
     * Returns the avatar URL in the response.
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> result = avatarService.uploadAvatar(username, file);

        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            return ResponseEntity.ok(ApiResult.success(result));
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/contacts"))
                .build();
    }
}