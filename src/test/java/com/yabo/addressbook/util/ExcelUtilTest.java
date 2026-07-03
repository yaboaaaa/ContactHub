package com.yabo.addressbook.util;

import com.yabo.addressbook.entity.Contact;
import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExcelUtilTest {

    @Mock
    private HttpServletResponse response;

    @Test
    void exportContacts_shouldCreateValidXLSX() throws Exception {
        User user = new User();
        user.setId(1L);

        ContactGroup group = new ContactGroup();
        group.setId(1L);
        group.setName("同事");

        Contact contact = new Contact();
        contact.setId(1L);
        contact.setName("张三");
        contact.setFamilyName("张");
        contact.setGivenName("三");
        contact.setUid("test-uid-123");
        contact.setGender(1);
        contact.setPhoneMobile("13800138000");
        contact.setEmail("zhangsan@example.com");
        contact.setCompany("科技公司");
        contact.setJobTitle("工程师");
        contact.setProvince("广东省");
        contact.setCity("深圳市");
        contact.setBirthday(LocalDate.of(1990, 1, 1));
        contact.setGroup(group);
        contact.setCreatedAt(LocalDateTime.now());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ServletOutputStream servletOutputStream = mock(ServletOutputStream.class);
        doAnswer(invocation -> {
            byte[] buf = invocation.getArgument(0);
            int offset = invocation.getArgument(1);
            int len = invocation.getArgument(2);
            outputStream.write(buf, offset, len);
            return null;
        }).when(servletOutputStream).write(any(byte[].class), anyInt(), anyInt());

        when(response.getOutputStream()).thenReturn(servletOutputStream);

        ExcelUtil.exportContacts(List.of(contact), response);

        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setHeader(eq("Content-Disposition"), anyString());

        // Verify the output is a valid XLSX
        byte[] resultBytes = outputStream.toByteArray();
        assertThat(resultBytes).isNotEmpty();

        // Verify we can read it back
        XSSFWorkbook readBack = new XSSFWorkbook(new ByteArrayInputStream(resultBytes));
        Sheet sheet = readBack.getSheetAt(0);
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("姓");
        assertThat(sheet.getRow(0).getCell(2).getStringCellValue()).isEqualTo("显示名");
        assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("张");
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("三");
        assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo("张三");
        assertThat(sheet.getRow(1).getCell(3).getStringCellValue()).isEqualTo("男");
        assertThat(sheet.getRow(1).getCell(16).getStringCellValue()).isEqualTo("同事");
        readBack.close();
    }
}