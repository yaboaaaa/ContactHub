package com.yabo.addressbook.controller;

import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.GroupService;
import com.yabo.addressbook.service.LoginAttemptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@WithMockUser(username = "testuser")
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

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
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    }

    @Test
    void listGroups_shouldReturnGroups() throws Exception {
        when(groupService.listGroups(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void createGroup_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/groups")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"新分组\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(groupService).createGroup(1L, "新分组");
    }

    @Test
    void updateGroup_shouldReturnSuccess() throws Exception {
        mockMvc.perform(put("/api/v1/groups/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"改名\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(groupService).updateGroup(1L, 1L, "改名");
    }

    @Test
    void deleteGroup_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/groups/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(groupService).deleteGroup(1L, 1L);
    }
}
