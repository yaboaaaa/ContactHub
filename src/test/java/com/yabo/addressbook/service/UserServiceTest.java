package com.yabo.addressbook.service;

import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactGroupRepository contactGroupRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, contactGroupRepository, passwordEncoder);
    }

    @Test
    void register_shouldCreateUserAndDefaultGroup_whenUsernameIsUnique() {
        String username = "newuser";
        String password = "password123";
        String email = "newuser@example.com";
        String encodedPassword = "encodedPassword123";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.existsByUsername(username)).thenReturn(false);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(username);
        savedUser.setPassword(encodedPassword);
        savedUser.setEmail(email);
        savedUser.setRole("USER");
        savedUser.setEnabled(true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        ContactGroup savedGroup = new ContactGroup();
        savedGroup.setId(1L);
        savedGroup.setName("默认分组");
        savedGroup.setIsDefault(true);
        when(contactGroupRepository.save(any(ContactGroup.class))).thenReturn(savedGroup);

        User result = userService.register(username, password, email);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo(email);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo(username);
        assertThat(capturedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(capturedUser.getEmail()).isEqualTo(email);
        assertThat(capturedUser.getRole()).isEqualTo("USER");
        assertThat(capturedUser.getEnabled()).isTrue();

        ArgumentCaptor<ContactGroup> groupCaptor = ArgumentCaptor.forClass(ContactGroup.class);
        verify(contactGroupRepository).save(groupCaptor.capture());
        ContactGroup capturedGroup = groupCaptor.getValue();
        assertThat(capturedGroup.getName()).isEqualTo("默认分组");
        assertThat(capturedGroup.getIsDefault()).isTrue();
        assertThat(capturedGroup.getUser()).isEqualTo(savedUser);
        assertThat(capturedGroup.getSortOrder()).isZero();

        verify(userRepository).existsByUsername(username);
        verify(passwordEncoder).encode(password);
        verifyNoMoreInteractions(userRepository, contactGroupRepository, passwordEncoder);
    }

    @Test
    void register_shouldThrowBusinessException_whenUsernameAlreadyExists() {
        String username = "existinguser";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThatThrownBy(() -> userService.register(username, "password", "email@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");

        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).save(any(User.class));
        verify(contactGroupRepository, never()).save(any(ContactGroup.class));
    }

    @Test
    void findByUsername_shouldReturnUser_whenFound() {
        String username = "testuser";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword("password");
        user.setRole("USER");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername(username);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(username);

        assertThat(result).isEmpty();
    }
}