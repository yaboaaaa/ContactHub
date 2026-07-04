package com.yabo.addressbook.service;

import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.exception.BusinessException;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.ContactRepository;
import com.yabo.addressbook.repository.UserRepository;
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
        verify(userRepository).findAll();
    }

    @Test
    void createUser_shouldCreateUserWithRoleUSER() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = adminService.createUser("newuser", "password123", "new@example.com");

        assertThat(result).isNotNull();
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrow_whenUsernameExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser("existing", "password123", "e@t.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");
    }

    @Test
    void deleteUser_shouldThrow_whenDeletingAdmin() {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole("ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> adminService.deleteUser(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不能删除管理员账号");
    }

    @Test
    void deleteUser_shouldCascadeDelete() {
        User normalUser = new User();
        normalUser.setId(2L);
        normalUser.setRole("USER");
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

        adminService.deleteUser(2L);

        verify(contactRepository).deleteByUserId(2L);
        verify(contactGroupRepository).deleteByUserId(2L);
        verify(userRepository).delete(normalUser);
    }

    @Test
    void deleteUser_shouldThrow_whenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.deleteUser(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
    }

    @Test
    void updateUser_shouldUpdateEmailAndEnabled() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEnabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = adminService.updateUser(1L, null, null, "new@example.com", false);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    void updateUser_shouldUpdateNickname() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = adminService.updateUser(1L, null, "新昵称", null, null);

        assertThat(result.getNickname()).isEqualTo("新昵称");
    }

    @Test
    void updateUser_shouldUpdateUsername() {
        User user = new User();
        user.setId(1L);
        user.setUsername("oldname");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = adminService.updateUser(1L, "newname", null, null, null);

        assertThat(result.getUsername()).isEqualTo("newname");
    }

    @Test
    void updateUser_shouldThrow_whenUsernameTaken() {
        User user = new User();
        user.setId(1L);
        user.setUsername("oldname");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> adminService.updateUser(1L, "taken", null, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");
    }

    @Test
    void updateUser_shouldThrow_whenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.updateUser(999L, null, null, "e@t.com", true))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
    }

    @Test
    void resetPassword_shouldUpdatePassword() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        adminService.resetPassword(1L, "newpass");

        assertThat(user.getPassword()).isEqualTo("encoded_new");
    }

    @Test
    void resetPassword_shouldThrow_whenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.resetPassword(999L, "pass"))
                .isInstanceOf(BusinessException.class);
    }
}
