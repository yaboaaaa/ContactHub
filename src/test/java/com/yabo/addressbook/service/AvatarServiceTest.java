package com.yabo.addressbook.service;

import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AvatarServiceTest {

    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private AvatarService avatarService;

    private User user;
    private String username;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        avatarService = new AvatarService(userRepository, tempDir.toString());
        username = "testuser";

        user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword("password");
        user.setRole("USER");
    }

    private byte[] createValidPngBytes() throws Exception {
        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    @Test
    void uploadAvatar_shouldSaveCompressedFileAndUpdateUser() throws Exception {
        byte[] avatarBytes = createValidPngBytes();

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn((long) avatarBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(avatarBytes));
        when(file.getOriginalFilename()).thenReturn("test.png");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Map<String, Object> result = avatarService.uploadAvatar(username, file);

        // Verify user was saved with avatarUrl set
        verify(userRepository).save(user);
        assertThat(user.getAvatarUrl()).isNotNull();
        assertThat(user.getAvatarUrl()).endsWith(".png");
        assertThat(user.getUpdatedAt()).isNotNull();

        // Verify compressed file was written to disk
        String storedUrl = user.getAvatarUrl();
        String filename = storedUrl.substring("/uploads/avatars/".length());
        Path savedFile = tempDir.resolve(filename);
        assertThat(Files.exists(savedFile)).isTrue();

        // Verify result has url and size
        assertThat(result.get("url")).isEqualTo(user.getAvatarUrl());
        assertThat(result.get("size")).isNotNull();
    }

    @Test
    void uploadAvatar_shouldDeleteOldAvatarFile() throws Exception {
        // Set existing avatar
        String oldFilename = "old-avatar.png";
        Path oldFile = tempDir.resolve(oldFilename);
        Files.write(oldFile, "old-data".getBytes());
        user.setAvatarUrl("/uploads/avatars/" + oldFilename);

        byte[] newBytes = createValidPngBytes();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(newBytes));
        when(file.getOriginalFilename()).thenReturn("new.png");
        when(file.getSize()).thenReturn((long) newBytes.length);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        avatarService.uploadAvatar(username, file);

        // Old file should be deleted
        assertThat(Files.exists(oldFile)).isFalse();
    }

    @Test
    void uploadAvatar_shouldThrowBusinessException_whenUserNotFound() {
        String nonExistentUser = "nonexistent";
        MultipartFile file = mock(MultipartFile.class);
        when(userRepository.findByUsername(nonExistentUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avatarService.uploadAvatar(nonExistentUser, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void uploadAvatar_shouldRejectInvalidContentType() throws Exception {
        byte[] avatarBytes = createValidPngBytes();

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/gif");
        when(file.getSize()).thenReturn((long) avatarBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(avatarBytes));
        when(file.getOriginalFilename()).thenReturn("test.gif");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> avatarService.uploadAvatar(username, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅支持");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void uploadAvatar_shouldRejectInvalidExtension() throws Exception {
        byte[] avatarBytes = createValidPngBytes();

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn((long) avatarBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(avatarBytes));
        when(file.getOriginalFilename()).thenReturn("test.bmp");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> avatarService.uploadAvatar(username, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅支持");

        verify(userRepository, never()).save(any(User.class));
    }
}