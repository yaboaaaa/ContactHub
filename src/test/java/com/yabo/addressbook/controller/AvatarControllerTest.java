package com.yabo.addressbook.controller;

import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.AvatarService;
import com.yabo.addressbook.service.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvatarController.class)
@WithMockUser(username = "testuser")
class AvatarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvatarService avatarService;

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
        user.setAvatarUrl("/uploads/avatars/test.jpg");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @Test
    void getCurrentUserAvatar_shouldReturnUrl() throws Exception {
        mockMvc.perform(get("/api/v1/user/avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.url").value("/uploads/avatars/test.jpg"));
    }

    @Test
    void getCurrentUserAvatar_shouldReturnNull_whenNoAvatar() throws Exception {
        User noAvatar = new User();
        noAvatar.setUsername("testuser");
        noAvatar.setAvatarUrl(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(noAvatar));

        mockMvc.perform(get("/api/v1/user/avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value((String) null));
    }

    @Test
    void getUserAvatarById_shouldReturnUrl() throws Exception {
        mockMvc.perform(get("/api/v1/user/1/avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.url").value("/uploads/avatars/test.jpg"));
    }

    @Test
    void getUserAvatarById_shouldReturnNull_whenNotFound() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/user/999/avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value((String) null));
    }
}
