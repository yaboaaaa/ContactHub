package com.yabo.addressbook.service;

import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

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

    @Test
    void uploadAvatar_shouldSaveFileAndUpdateUser() throws Exception {
        byte[] avatarBytes = "fake-image-data".getBytes();
        String contentType = "image/png";

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(contentType);
        when(file.getSize()).thenReturn((long) avatarBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(avatarBytes));
        when(file.getOriginalFilename()).thenReturn("test.png");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        avatarService.uploadAvatar(username, file);

        // Verify user was saved with avatarPath set
        verify(userRepository).save(user);
        assertThat(user.getAvatarPath()).isNotNull();
        assertThat(user.getAvatarPath()).endsWith(".png");
        assertThat(user.getAvatarContentType()).isEqualTo(contentType);
        assertThat(user.getUpdatedAt()).isNotNull();

        // Verify file was written to disk
        Path savedFile = tempDir.resolve(user.getAvatarPath());
        assertThat(Files.exists(savedFile)).isTrue();
        assertThat(Files.readAllBytes(savedFile)).isEqualTo(avatarBytes);
    }

    @Test
    void uploadAvatar_shouldDeleteOldAvatarFile() throws Exception {
        // Set existing avatar
        String oldFilename = "old-avatar.png";
        Path oldFile = tempDir.resolve(oldFilename);
        Files.write(oldFile, "old-data".getBytes());
        user.setAvatarPath(oldFilename);

        byte[] newBytes = "new-image-data".getBytes();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(newBytes));
        when(file.getOriginalFilename()).thenReturn("new.png");
        when(file.getSize()).thenReturn((long) newBytes.length);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        avatarService.uploadAvatar(username, file);

        // Old file should be deleted
        assertThat(Files.exists(oldFile)).isFalse();
        // New file should exist
        Path newFile = tempDir.resolve(user.getAvatarPath());
        assertThat(Files.exists(newFile)).isTrue();
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
    void getAvatar_shouldReturnFileBytes() throws Exception {
        byte[] expectedBytes = "avatar-content".getBytes();
        String filename = "avatar-" + UUID.randomUUID() + ".png";
        Files.write(tempDir.resolve(filename), expectedBytes);
        user.setAvatarPath(filename);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        byte[] result = avatarService.getAvatar(username);

        assertThat(result).isEqualTo(expectedBytes);
    }

    @Test
    void getAvatar_shouldReturnNull_whenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        byte[] result = avatarService.getAvatar("nonexistent");

        assertThat(result).isNull();
    }

    @Test
    void getAvatar_shouldReturnNull_whenNoAvatarPath() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        byte[] result = avatarService.getAvatar(username);

        assertThat(result).isNull();
    }

    @Test
    void getAvatar_shouldReturnNull_whenFileDoesNotExist() {
        user.setAvatarPath("nonexistent.png");
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        byte[] result = avatarService.getAvatar(username);

        assertThat(result).isNull();
    }

    @Test
    void getAvatarById_shouldReturnFileBytes() throws Exception {
        byte[] expectedBytes = "avatar-by-id".getBytes();
        String filename = "avatar-" + UUID.randomUUID() + ".png";
        Files.write(tempDir.resolve(filename), expectedBytes);
        user.setAvatarPath(filename);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        byte[] result = avatarService.getAvatarById(1L);

        assertThat(result).isEqualTo(expectedBytes);
    }

    @Test
    void getAvatarById_shouldReturnNull_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        byte[] result = avatarService.getAvatarById(999L);

        assertThat(result).isNull();
    }

    @Test
    void getAvatarContentType_shouldReturnContentType() {
        String expectedContentType = "image/png";
        user.setAvatarContentType(expectedContentType);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        String result = avatarService.getAvatarContentType(username);

        assertThat(result).isEqualTo(expectedContentType);
    }

    @Test
    void getAvatarContentType_shouldReturnNull_whenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        String result = avatarService.getAvatarContentType("nonexistent");

        assertThat(result).isNull();
    }
}