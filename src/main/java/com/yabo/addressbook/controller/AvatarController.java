package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ApiResult;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.AvatarService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

@Controller
@RequestMapping("/user")
public class AvatarController {

    private static final byte[] DEFAULT_AVATAR = createDefaultAvatar();

    private final AvatarService avatarService;
    private final UserRepository userRepository;

    public AvatarController(AvatarService avatarService, UserRepository userRepository) {
        this.avatarService = avatarService;
        this.userRepository = userRepository;
    }

    /**
     * Get avatar for the currently authenticated user.
     */
    @GetMapping("/avatar")
    public ResponseEntity<byte[]> getCurrentUserAvatar() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(DEFAULT_AVATAR);
        }
        return buildAvatarResponse(user);
    }

    /**
     * Get avatar for a specific user by user id.
     */
    @GetMapping("/{id}/avatar")
    public ResponseEntity<byte[]> getUserAvatarById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(DEFAULT_AVATAR);
        }
        return buildAvatarResponse(user);
    }

    /**
     * Upload avatar for the currently authenticated user.
     * Returns JSON for AJAX requests, redirects to /contacts for form submissions.
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                          HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        avatarService.uploadAvatar(username, file);

        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            return ResponseEntity.ok(ApiResult.success());
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/contacts"))
                .build();
    }

    /**
     * Build avatar response for a user entity.
     * Prioritizes avatarUrl (external URL) over avatarData (stored bytes).
     */
    private ResponseEntity<byte[]> buildAvatarResponse(User user) {
        // If avatarUrl is set, redirect to the external URL
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(user.getAvatarUrl()))
                    .build();
        }

        // If avatarData is stored locally, return it with proper content type
        if (user.getAvatarData() != null && user.getAvatarData().length > 0) {
            MediaType mediaType = user.getAvatarContentType() != null
                    ? MediaType.parseMediaType(user.getAvatarContentType())
                    : MediaType.IMAGE_PNG;
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(user.getAvatarData());
        }

        // Default placeholder
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(DEFAULT_AVATAR);
    }

    /**
     * Create a simple default avatar image (gray circle with "?").
     */
    private static byte[] createDefaultAvatar() {
        try {
            BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(0, 0, 200, 200);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 80));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString("?", (200 - fm.stringWidth("?")) / 2,
                    (200 - fm.getAscent()) / 2 + fm.getAscent());
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }
}