package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.entity.Contact;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.ContactRepository;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.util.InitialsAvatarUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/contacts")
public class ContactAvatarController {

    private static final Logger log = LoggerFactory.getLogger(ContactAvatarController.class);

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactAvatarController(ContactRepository contactRepository,
                                    UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get avatar for a contact by contact id.
     * If the contact has a custom avatarUrl, return the URL as JSON.
     * Otherwise, generate and return an initials-based PNG avatar.
     */
    @GetMapping("/{id}/avatar")
    public ResponseEntity<?> getContactAvatar(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId();

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "联系人不存在"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该联系人");
        }

        // If contact has a custom avatar, return the URL as JSON (for AJAX requests)
        if (contact.getAvatarUrl() != null && !contact.getAvatarUrl().isEmpty()) {
            return ResponseEntity.ok(ApiResult.success(Map.of("url", contact.getAvatarUrl())));
        }

        // Otherwise generate initials avatar as binary image
        byte[] avatarBytes = InitialsAvatarUtil.generateAvatar(contact);

        CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS)
                .cachePublic();

        String etag = "\"contact-avatar-" + id + "\"";
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .cacheControl(cacheControl)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(cacheControl)
                .eTag(etag)
                .body(avatarBytes);
    }

    /**
     * Upload avatar for a contact.
     * Saves the file to disk and stores the URL in the contact entity.
     */
    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> uploadContactAvatar(@PathVariable Long id,
                                                  @RequestParam("file") MultipartFile file,
                                                  HttpServletRequest request) {
        Long userId = getCurrentUserId();

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "联系人不存在"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权操作该联系人");
        }

        try {
            Path uploadDir = Paths.get("uploads/avatars/contacts").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            if (extension.isEmpty()) {
                extension = ".png";
            }
            String filename = "contact-" + id + "-" + UUID.randomUUID().toString() + extension;

            Path targetPath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath);

            // Delete old avatar file
            String oldUrl = contact.getAvatarUrl();
            if (oldUrl != null && oldUrl.startsWith("/uploads/avatars/contacts/")) {
                String oldFilename = oldUrl.substring("/uploads/avatars/contacts/".length());
                try {
                    Files.deleteIfExists(uploadDir.resolve(oldFilename));
                } catch (IOException ignored) {}
            }

            String avatarUrl = "/uploads/avatars/contacts/" + filename;
            contact.setAvatarUrl(avatarUrl);
            contactRepository.save(contact);

            String requestedWith = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith)) {
                return ResponseEntity.ok(ApiResult.success(Map.of("url", avatarUrl)));
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/contacts"))
                    .build();
        } catch (IOException e) {
            log.error("Failed to upload contact avatar: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResult.error(400, "头像上传失败"));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未登录");
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
        return user.getId();
    }
}