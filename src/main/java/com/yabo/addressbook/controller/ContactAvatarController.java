package com.yabo.addressbook.controller;

import com.yabo.addressbook.entity.Contact;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.ContactRepository;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.util.InitialsAvatarUtil;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/contacts")
public class ContactAvatarController {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactAvatarController(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<?> getContactAvatar(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId();

        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "联系人不存在"));

        if (!contact.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该联系人");
        }

        if (contact.getAvatarUrl() != null && !contact.getAvatarUrl().isEmpty()) {
            return ResponseEntity.ok(Map.of("url", contact.getAvatarUrl()));
        }

        byte[] avatarBytes = InitialsAvatarUtil.generateAvatar(contact);

        CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic();
        String etag = "\"contact-avatar-" + id + "\"";
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .cacheControl(cacheControl).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(cacheControl).eTag(etag)
                .body(avatarBytes);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户未登录");
        }
        Object principal = authentication.getPrincipal();
        String username = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername() : principal.toString();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
        return user.getId();
    }
}
