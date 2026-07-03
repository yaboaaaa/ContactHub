package com.yabo.addressbook.controller;

import com.yabo.addressbook.dto.ContactDTO;
import com.yabo.addressbook.dto.ImportResult;
import com.yabo.addressbook.dto.PageDTO;
import com.yabo.addressbook.entity.Contact;
import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.ContactRepository;
import com.yabo.addressbook.repository.UserRepository;
import com.yabo.addressbook.service.ContactService;
import com.yabo.addressbook.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@WithMockUser(username = "testuser")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactService contactService;

    @MockBean
    private GroupService groupService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ContactRepository contactRepository;

    @MockBean
    private ContactGroupRepository contactGroupRepository;

    private User currentUser;
    private ContactGroup defaultGroup;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");
        currentUser.setRole("USER");

        defaultGroup = new ContactGroup();
        defaultGroup.setId(1L);
        defaultGroup.setName("默认分组");
        defaultGroup.setIsDefault(true);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
    }

    @Test
    void getContacts_shouldReturnContactsView() throws Exception {
        when(groupService.listGroups(1L)).thenReturn(List.of(defaultGroup));

        mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("groups"));
    }

    @Test
    void postContacts_shouldCreateContact() throws Exception {
        ContactDTO dto = new ContactDTO();
        dto.setName("张三");
        dto.setPhoneMobile("13800138000");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/contacts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(contactService).createContact(any(ContactDTO.class), eq(1L));
    }

    @Test
    void putContacts_shouldUpdateContact() throws Exception {
        Long contactId = 1L;
        ContactDTO dto = new ContactDTO();
        dto.setName("李四");
        dto.setPhoneMobile("13900139000");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/contacts/{id}", contactId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(contactService).updateContact(eq(contactId), any(ContactDTO.class), eq(1L));
    }

    @Test
    void deleteContacts_shouldSoftDelete() throws Exception {
        Long contactId = 1L;

        mockMvc.perform(delete("/contacts/{id}", contactId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(contactService).softDelete(contactId, 1L);
    }

    @Test
    void getContactsData_shouldReturnPaginatedData() throws Exception {
        Page<Contact> page = new PageImpl<>(List.of());
        when(contactService.searchContacts(anyLong(), any(), any(), any(), any(),
                any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/contacts/data")
                        .param("keyword", "测试")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    void getContactsData_shouldReturnDataWithoutOptionalParams() throws Exception {
        Page<Contact> page = new PageImpl<>(List.of());
        when(contactService.searchContacts(anyLong(), any(), any(), any(), any(),
                any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/contacts/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void restoreContact_shouldRestoreContact() throws Exception {
        Long contactId = 1L;

        mockMvc.perform(put("/contacts/{id}/restore", contactId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(contactService).restore(contactId, 1L);
    }

    @Test
    void permanentDeleteContact_shouldPermanentlyDelete() throws Exception {
        Long contactId = 1L;

        mockMvc.perform(delete("/contacts/{id}/permanent", contactId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(contactService).permanentDelete(contactId, 1L);
    }

    @Test
    void emptyRecycleBin_shouldEmptyRecycleBin() throws Exception {
        mockMvc.perform(delete("/contacts/recycle")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(contactService).emptyRecycleBin(1L);
    }

    @Test
    void recyclePage_shouldReturnRecycleView() throws Exception {
        mockMvc.perform(get("/contacts/recycle"))
                .andExpect(status().isOk())
                .andExpect(view().name("recycle"));
    }

    @Test
    void getRecycleData_shouldReturnRecycleData() throws Exception {
        Page<Contact> page = new PageImpl<>(List.of());
        when(contactService.getRecycleBin(1L, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/contacts/recycle/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private byte[] createValidXlsxFile() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("通讯录");
        Row header = sheet.createRow(0);
        String[] headers = {"姓名", "性别", "手机", "家庭电话", "工作电话", "邮箱", "单位", "职位",
                "省", "市", "区", "详细地址", "生日", "备注", "所属分组"};
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("测试联系人");
        dataRow.createCell(2).setCellValue("13800138000");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    @Test
    void importContacts_shouldImportFile() throws Exception {
        byte[] validXlsxData = createValidXlsxFile();
        MockMultipartFile file = new MockMultipartFile(
                "file", "contacts.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                validXlsxData);

        ImportResult importResult = new ImportResult(1, 0, List.of());
        when(contactGroupRepository.findByUserIdAndIsDefaultTrue(anyLong()))
                .thenReturn(Optional.of(defaultGroup));

        mockMvc.perform(multipart("/contacts/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}