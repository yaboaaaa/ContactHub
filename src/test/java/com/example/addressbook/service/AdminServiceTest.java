package com.example.addressbook.service;

import com.example.addressbook.entity.User;
import com.example.addressbook.exception.BusinessException;
import com.example.addressbook.repository.ContactGroupRepository;
import com.example.addressbook.repository.ContactRepository;
import com.example.addressbook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactGroupRepository contactGroupRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, contactGroupRepository, contactRepository, passwordEncoder);
    }

    @Test
    void listUsers_shouldReturnAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setRole("USER");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setRole("USER");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> result = adminService.listUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("user1");
        assertThat(result.get(1).getUsername()).isEqualTo("user2");
        verify(userRepository).findAll();
    }

    @Test
    void createUser_shouldCreateUserWithRoleUSER() {
        String username = "newuser";
        String password = "password123";
        String email = "newuser@example.com";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encoded");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(username);
        savedUser.setPassword("encoded");
        savedUser.setEmail(email);
        savedUser.setRole("USER");
        savedUser.setEnabled(true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = adminService.createUser(username, password, email);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getEnabled()).isTrue();
        verify(userRepository).existsByUsername(username);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowBusinessException_whenUsernameExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser("existing", "pass", "email@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_shouldThrowBusinessException_whenDeletingAdmin() {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> adminService.deleteUser(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不能删除管理员账号");

        verify(contactRepository, never()).deleteByUserId(anyLong());
        verify(contactGroupRepository, never()).deleteByUserId(anyLong());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_shouldCascadeDeleteContactsAndGroups() {
        User normalUser = new User();
        normalUser.setId(2L);
        normalUser.setUsername("normaluser");
        normalUser.setRole("USER");

        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

        adminService.deleteUser(2L);

        verify(contactRepository).deleteByUserId(2L);
        verify(contactGroupRepository).deleteByUserId(2L);
        verify(userRepository).delete(normalUser);
    }

    @Test
    void deleteUser_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
    }

    @Test
    void toggleEnabled_shouldToggleEnabledStatus() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEnabled(true);
        user.setRole("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User toggled1 = adminService.toggleEnabled(1L);
        assertThat(toggled1.getEnabled()).isFalse();

        User toggled2 = adminService.toggleEnabled(1L);
        assertThat(toggled2.getEnabled()).isTrue();

        verify(userRepository, times(2)).save(user);
    }

    @Test
    void toggleEnabled_shouldHandleNullEnabled() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEnabled(null);
        user.setRole("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = adminService.toggleEnabled(1L);

        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    void toggleEnabled_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.toggleEnabled(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
    }

    @Test
    void updateUser_shouldUpdateEmailAndEnabled() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("old@example.com");
        user.setEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = adminService.updateUser(1L, "new@example.com", false);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    void updateUser_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.updateUser(999L, "email", true))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
    }
}