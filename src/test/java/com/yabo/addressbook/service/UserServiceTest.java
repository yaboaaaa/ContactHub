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

        User result = userService.register(username, password, email, "我的昵称");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo(email);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo(username);
        assertThat(capturedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(capturedUser.getEmail()).isEqualTo(email);
        assertThat(capturedUser.getNickname()).isEqualTo("我的昵称");
        assertThat(capturedUser.getRole()).isEqualTo("USER");
        assertThat(capturedUser.getEnabled()).isTrue();

        verify(userRepository).existsByUsername(username);
        verify(passwordEncoder).encode(password);
    }

    @Test
    void register_shouldGenerateRandomNickname_whenNicknameIsNull() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(contactGroupRepository.save(any(ContactGroup.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.register("testuser", "password123", "t@t.com", null);

        assertThat(result.getNickname()).isNotNull();
        assertThat(result.getNickname()).isNotEmpty();
    }

    @Test
    void register_shouldThrowException_whenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("existinguser", "password", "email@test.com", null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenUsernameInvalid() {
        assertThatThrownBy(() -> userService.register("ab", "password", null, null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> userService.register("1abc", "password", null, null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> userService.register("user_name", "password", null, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void register_shouldThrowException_whenPasswordTooShort() {
        assertThatThrownBy(() -> userService.register("validuser", "12345", null, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateProfile_shouldUpdateFields() {
        User user = new User();
        user.setId(1L);
        user.setUsername("oldname");
        user.setEmail("old@test.com");
        user.setNickname("oldnick");

        when(userRepository.findByUsername("oldname")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updateProfile("oldname", "newname", "new@test.com", "新昵称");

        assertThat(user.getUsername()).isEqualTo("newname");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
        assertThat(user.getNickname()).isEqualTo("新昵称");
    }

    @Test
    void updatePassword_shouldUpdatePassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded_old");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", "encoded_old")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded_new");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updatePassword("testuser", "oldpass", "newpass");

        assertThat(user.getPassword()).isEqualTo("encoded_new");
    }

    @Test
    void updatePassword_shouldThrow_whenOldPasswordWrong() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encoded_old");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded_old")).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword("testuser", "wrong", "newpass"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("原密码错误");
    }

    @Test
    void generateRandomNickname_shouldReturnNonEmpty() {
        String nick = userService.generateRandomNickname();
        assertThat(nick).isNotNull();
        assertThat(nick.length()).isGreaterThan(4);
    }

    @Test
    void findByUsername_shouldReturnUser_whenFound() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertThat(userService.findByUsername("nonexistent")).isEmpty();
    }
}
