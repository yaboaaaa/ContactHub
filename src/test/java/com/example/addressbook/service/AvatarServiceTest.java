package com.example.addressbook.service;

import com.example.addressbook.entity.User;
import com.example.addressbook.exception.BusinessException;
import com.example.addressbook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock
    private UserRepository userRepository;

    private AvatarService avatarService;

    private User user;
    private String username;

    @BeforeEach
    void setUp() {
        avatarService = new AvatarService(userRepository);
        username = "testuser";

        user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword("password");
        user.setRole("USER");
    }

    @Test
    void uploadAvatar_shouldResizeAndSaveAvatar() {
        byte[] avatarBytes = new byte[]{1, 2, 3, 4, 5};
        String contentType = "image/jpeg";

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(contentType);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        avatarService.uploadAvatar(username, file);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getAvatarData()).isNotNull();
        assertThat(savedUser.getAvatarContentType()).isEqualTo(contentType);
        assertThat(savedUser.getUpdatedAt()).isNotNull();
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
    void getAvatar_shouldReturnAvatarBytes() {
        byte[] expectedBytes = new byte[]{10, 20, 30};
        user.setAvatarData(expectedBytes);

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
    void getAvatar_shouldReturnNull_whenNoAvatarData() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        byte[] result = avatarService.getAvatar(username);

        assertThat(result).isNull();
    }

    @Test
    void getAvatarById_shouldReturnAvatarBytes() {
        byte[] expectedBytes = new byte[]{10, 20, 30};
        user.setAvatarData(expectedBytes);

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