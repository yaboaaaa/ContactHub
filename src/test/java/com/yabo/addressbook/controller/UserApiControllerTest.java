package com.yabo.addressbook.controller;

import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserApiController.class)
@WithMockUser(username = "testuser")
class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LoginAttemptService loginAttemptService;

    @MockBean
    private com.yabo.addressbook.service.CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setNickname("测试用户");
        user.setRole("USER");
        user.setAvatarUrl("/uploads/test.jpg");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    }

    @Test
    void getCurrentUser_shouldReturnUserInfo() throws Exception {
        mockMvc.perform(get("/api/v1/user/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                .andExpect(jsonPath("$.data.isAdmin").value(false))
                .andExpect(jsonPath("$.data.avatarUrl").value("/uploads/test.jpg"));
    }

    @Test
    void getCurrentUser_shouldReturnAdmin_whenRoleAdmin() throws Exception {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole("ADMIN");
        admin.setNickname("管理员");
        when(userRepository.findByUsername("testuser")).thenReturn(
                Optional.of(new User() {{ setId(1L); setUsername("testuser"); setRole("ADMIN"); setNickname("管理员"); }}));

        mockMvc.perform(get("/api/v1/user/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isAdmin").value(true));
    }
}
