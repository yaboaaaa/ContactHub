package com.yabo.addressbook.controller;

import com.yabo.addressbook.service.CaptchaService;
import com.yabo.addressbook.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegisterController.class)
class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CaptchaService captchaService;

    @Test
    void checkUsername_shouldReturnBoolean() throws Exception {
        when(userService.existsByUsername("testuser")).thenReturn(false);

        mockMvc.perform(get("/api/v1/register/check-username").param("username", "testuser"))
                .andExpect(status().isOk());
    }
}
