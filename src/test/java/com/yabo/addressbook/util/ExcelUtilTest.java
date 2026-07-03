package com.yabo.addressbook.util;

import com.yabo.addressbook.dto.ImportResult;
import com.yabo.addressbook.entity.Contact;
import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.ContactRepository;
import com.yabo.addressbook.repository.UserRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelUtilTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactGroupRepository contactGroupRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private HttpServletResponse response;

    private static final String[] NEW_HEADERS = {
            "UID", "姓", "名", "显示名", "性别", "手机", "家庭电话", "工作电话",
            "邮箱", "单位", "职位", "省", "市", "区", "详细地址", "生日", "备注", "所属分组"
    };

    private static final String[] LEGACY_HEADERS = {
            "姓名", "性别", "手机", "家庭电话", "工作电话", "邮箱", "单位", "职位",
            "省", "市", "区", "详细地址", "生日", "备注", "所属分组"
    };

    /**
     * Create a test XLSX workbook using the new column headers.
     */
    private byte[] createTestWorkbook(String... rowData) throws IOException {
        return createWorkbookWithHeaders(NEW_HEADERS, rowData);
    }

    /**
     * Create a test XLSX workbook using the legacy column headers.
     */
    private byte[] createLegacyTestWorkbook(String... rowData) throws IOException {
        return createWorkbookWithHeaders(LEGACY_HEADERS, rowData);
    }

    private byte[] createWorkbookWithHeaders(String[] headers, String... rowData) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("通讯录");

        // Header row
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        if (rowData.length > 0) {
            String[] values = rowData[0].split(",", -1);
            Row row = sheet.createRow(1);
            for (int i = 0; i < values.length && i < headers.length; i++) {
                row.createCell(i).setCellValue(values[i]);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

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
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("UID");
        assertThat(sheet.getRow(0).getCell(3).getStringCellValue()).isEqualTo("显示名");
        assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("test-uid-123");
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("张");
        assertThat(sheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo("三");
        assertThat(sheet.getRow(1).getCell(3).getStringCellValue()).isEqualTo("张三");
        assertThat(sheet.getRow(1).getCell(4).getStringCellValue()).isEqualTo("男");
        assertThat(sheet.getRow(1).getCell(17).getStringCellValue()).isEqualTo("同事");
        readBack.close();
    }

    @Test
    void importContacts_shouldParseRowsCorrectly() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        ContactGroup defaultGroup = new ContactGroup();
        defaultGroup.setId(1L);
        defaultGroup.setName("同事");

        byte[] workbookBytes = createTestWorkbook(
                ",张,三,张三,男,13800138000,01012345678,,zhangsan@test.com,公司A,经理,北京,北京市,朝阳区,某某路1号,1990-01-01,备注,同事");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) workbookBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(workbookBytes));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contactGroupRepository.findByUserIdAndName(eq(userId), eq("同事"))).thenReturn(Optional.of(defaultGroup));

        Contact savedContact = new Contact();
        savedContact.setId(1L);
        savedContact.setName("张三");
        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        ImportResult result = ExcelUtil.importContacts(file, userId, userRepository, contactGroupRepository, contactRepository);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isZero();
    }

    @Test
    void importContacts_shouldHandleBlankNameRows() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        byte[] workbookBytes = createTestWorkbook(",,,,男,13800138000,,,,,,,,,,,,,");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) workbookBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(workbookBytes));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ImportResult result = ExcelUtil.importContacts(file, userId, userRepository, contactGroupRepository, contactRepository);

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0)).contains("姓名为空");
    }

    @Test
    void importContacts_shouldAutoCreateGroups() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        byte[] workbookBytes = createTestWorkbook(
                ",李,四,李四,女,13900139000,,,,,,,,,,1995-05-15,,新分组");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) workbookBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(workbookBytes));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contactGroupRepository.findByUserIdAndName(eq(userId), eq("新分组"))).thenReturn(Optional.empty());
        when(contactGroupRepository.save(any(ContactGroup.class))).thenAnswer(invocation -> {
            ContactGroup g = invocation.getArgument(0);
            g.setId(2L);
            return g;
        });

        Contact savedContact = new Contact();
        savedContact.setId(1L);
        savedContact.setName("李四");
        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        ImportResult result = ExcelUtil.importContacts(file, userId, userRepository, contactGroupRepository, contactRepository);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        verify(contactGroupRepository).save(any(ContactGroup.class));
    }

    @Test
    void importContacts_shouldSupportLegacyTemplate() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        ContactGroup defaultGroup = new ContactGroup();
        defaultGroup.setId(1L);
        defaultGroup.setName("默认分组");
        defaultGroup.setIsDefault(true);

        byte[] workbookBytes = createLegacyTestWorkbook("测试联系人,男,13800138000,,,,,,,,,,,,");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn((long) workbookBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(workbookBytes));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(contactGroupRepository.findByUserIdAndIsDefaultTrue(userId)).thenReturn(Optional.of(defaultGroup));

        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ImportResult result = ExcelUtil.importContacts(file, userId, userRepository, contactGroupRepository, contactRepository);

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getFailCount()).isZero();
    }

    @Test
    void importContacts_shouldHandleEmptyFile() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        ImportResult result = ExcelUtil.importContacts(file, 1L, userRepository, contactGroupRepository, contactRepository);

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0)).contains("文件为空");
    }

    @Test
    void importContacts_shouldHandleNullFile() {
        ImportResult result = ExcelUtil.importContacts(null, 1L, userRepository, contactGroupRepository, contactRepository);

        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0)).contains("文件为空");
    }

    @Test
    void importContacts_shouldRejectLargeFile() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(11 * 1024 * 1024L); // > 10MB

        ImportResult result = ExcelUtil.importContacts(file, 1L, userRepository, contactGroupRepository, contactRepository);

        assertThat(result.getFailCount()).isEqualTo(1);
        assertThat(result.getFailures().get(0)).contains("文件过大");
    }
}
