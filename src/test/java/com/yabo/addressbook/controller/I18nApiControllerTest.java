package com.yabo.addressbook.controller;

import com.yabo.addressbook.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(I18nApiController.class)
@WithMockUser
class I18nApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginAttemptService loginAttemptService;

    @MockBean
    private com.yabo.addressbook.service.CaptchaService captchaService;

    @Test
    void getMessages_shouldReturnChineseByDefault() throws Exception {
        mockMvc.perform(get("/api/v1/i18n"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data['app.name']").value("通讯录系统"));
    }

    @Test
    void getMessages_shouldReturnEnglish_whenLangEn() throws Exception {
        mockMvc.perform(get("/api/v1/i18n").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data['app.name']").value("ContactHub"));
    }

    @Test
    void getMessages_shouldContainAllKeys() throws Exception {
        mockMvc.perform(get("/api/v1/i18n"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data['nav.contacts']").exists())
                .andExpect(jsonPath("$.data['login.btn']").exists())
                .andExpect(jsonPath("$.data['btn.save']").exists())
                .andExpect(jsonPath("$.data['contact.add']").exists())
                .andExpect(jsonPath("$.data['admin.title']").exists());
    }
}
