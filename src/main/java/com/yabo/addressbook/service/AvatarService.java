package com.yabo.addressbook.service;

import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AvatarService {

    private final UserRepository userRepository;
    private final Path uploadDir;
    private final String urlPrefix;

    public AvatarService(UserRepository userRepository,
                         @Value("${upload.avatar.dir:uploads/avatars}") String uploadDirStr) {
        this.userRepository = userRepository;
        this.uploadDir = Paths.get(uploadDirStr).toAbsolutePath().normalize();
        this.urlPrefix = "/uploads/avatars/";
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + this.uploadDir, e);
        }
    }

    /**
     * Upload avatar, save to filesystem, return the public URL.
     */
    public String uploadAvatar(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在: " + username));

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            if (extension.isEmpty()) {
                extension = ".png";
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Save file to disk
            Path targetPath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath);

            // Delete old avatar file if exists
            String oldUrl = user.getAvatarUrl();
            if (oldUrl != null && oldUrl.startsWith(urlPrefix)) {
                String oldFilename = oldUrl.substring(urlPrefix.length());
                try {
                    Files.deleteIfExists(uploadDir.resolve(oldFilename));
                } catch (IOException ignored) {}
            }

            // Build the public URL
            String avatarUrl = urlPrefix + filename;

            // Update user entity - store only the URL
            user.setAvatarUrl(avatarUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }
}