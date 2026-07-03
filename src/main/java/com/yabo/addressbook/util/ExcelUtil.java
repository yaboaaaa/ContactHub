package com.yabo.addressbook.util;

import com.yabo.addressbook.dto.ImportResult;
import com.yabo.addressbook.entity.Contact;
import com.yabo.addressbook.entity.ContactGroup;
import com.yabo.addressbook.entity.User;
import com.yabo.addressbook.repository.ContactGroupRepository;
import com.yabo.addressbook.repository.ContactRepository;
import com.yabo.addressbook.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExcelUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);
    private static final int MAX_ROWS = 5000;
    private static final String[] HEADERS = {
            "UID", "姓", "名", "显示名", "性别", "手机", "家庭电话", "工作电话",
            "邮箱", "单位", "职位", "省", "市", "区", "详细地址", "生日", "备注", "所属分组"
    };
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Export contacts to Excel and write to response output stream.
     */
    public static void exportContacts(List<Contact> contacts, HttpServletResponse response) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        try {
            Sheet sheet = workbook.createSheet("通讯录");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Contact contact : contacts) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                row.createCell(col++).setCellValue(nullToEmpty(contact.getUid()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getFamilyName()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getGivenName()));
                row.createCell(col++).setCellValue(contact.getDisplayName());
                row.createCell(col++).setCellValue(genderToString(contact.getGender()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getPhoneMobile()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getPhoneHome()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getPhoneWork()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getEmail()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getCompany()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getJobTitle()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getProvince()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getCity()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getDistrict()));
                row.createCell(col++).setCellValue(nullToEmpty(contact.getAddressDetail()));
                row.createCell(col++).setCellValue(contact.getBirthday() != null ? contact.getBirthday().format(DATE_FORMATTER) : "");
                row.createCell(col++).setCellValue(nullToEmpty(contact.getNotes()));
                row.createCell(col++).setCellValue(contact.getGroup() != null ? nullToEmpty(contact.getGroup().getName()) : "");
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String encodedFilename = URLEncoder.encode("contacts.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"contacts.xlsx\"; filename*=UTF-8''" + encodedFilename);

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("Failed to export contacts", e);
            throw new RuntimeException("导出联系人失败", e);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                log.error("Failed to close workbook", e);
            }
        }
    }

    /**
     * Import contacts from an Excel file.
     */
    public static ImportResult importContacts(MultipartFile file, Long userId,
                                               UserRepository userRepository,
                                               ContactGroupRepository groupRepository,
                                               ContactRepository contactRepository) {
        ImportResult result = new ImportResult();

        // Validate file
        if (file == null || file.isEmpty()) {
            result.addFailure("文件为空");
            return result;
        }

        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            result.addFailure("文件过大，请上传小于10MB的文件");
            return result;
        }

        XSSFWorkbook workbook = null;
        try (InputStream inputStream = file.getInputStream()) {
            workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            int lastRowNum = sheet.getLastRowNum();
            int rowsToProcess = Math.min(lastRowNum, MAX_ROWS);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // Parse header row to support both new and legacy templates
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.addFailure("Excel缺少表头行");
                return result;
            }
            ColumnIndex idx = new ColumnIndex(headerRow);

            // Start from row 1 (skip header row)
            for (int i = 1; i <= rowsToProcess; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    processRow(row, idx, user, groupRepository, contactRepository, result);
                } catch (Exception e) {
                    log.error("Error processing row {}: {}", i + 1, e.getMessage());
                    result.addFailure("第" + (i + 1) + "行处理失败: " + e.getMessage());
                }
            }

            if (lastRowNum > MAX_ROWS) {
                result.addFailure("最多支持导入" + MAX_ROWS + "条数据，超出部分已忽略");
            }

        } catch (IOException e) {
            log.error("Failed to read Excel file", e);
            result.addFailure("读取Excel文件失败: " + e.getMessage());
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.error("Failed to close workbook", e);
                }
            }
        }

        return result;
    }

    /**
     * Process a single row from the Excel sheet.
     */
    private static void processRow(Row row, ColumnIndex idx, User user,
                                    ContactGroupRepository groupRepository,
                                    ContactRepository contactRepository,
                                    ImportResult result) {
        String uid = idx.uid >= 0 ? getCellValue(row.getCell(idx.uid)) : "";
        String familyName = idx.familyName >= 0 ? getCellValue(row.getCell(idx.familyName)) : "";
        String givenName = idx.givenName >= 0 ? getCellValue(row.getCell(idx.givenName)) : "";
        String displayName = idx.displayName >= 0 ? getCellValue(row.getCell(idx.displayName)) : "";
        String legacyName = idx.name >= 0 ? getCellValue(row.getCell(idx.name)) : "";

        // Resolve name: prefer explicit split fields, then display name, then legacy name
        if (!StringUtils.hasText(familyName) && !StringUtils.hasText(givenName)) {
            String source = StringUtils.hasText(displayName) ? displayName : legacyName;
            if (StringUtils.hasText(source)) {
                String[] split = splitName(source.trim());
                familyName = split[0];
                givenName = split[1];
            }
        }

        String name = StringUtils.hasText(displayName) ? displayName.trim()
                : (familyName != null ? familyName : "") + (givenName != null ? givenName : "");

        if (!StringUtils.hasText(name)) {
            result.addFailure("第" + (row.getRowNum() + 1) + "行: 姓名为空，已跳过");
            return;
        }

        // Parse gender: 男/女/不便透露/未知 -> 1/2/0/0
        String genderStr = getCellValue(row.getCell(idx.gender));
        Integer gender = parseGender(genderStr);

        String phoneMobile = getCellValue(row.getCell(idx.phoneMobile));
        String phoneHome = getCellValue(row.getCell(idx.phoneHome));
        String phoneWork = getCellValue(row.getCell(idx.phoneWork));
        String email = getCellValue(row.getCell(idx.email));
        String company = getCellValue(row.getCell(idx.company));
        String jobTitle = getCellValue(row.getCell(idx.jobTitle));
        String province = getCellValue(row.getCell(idx.province));
        String city = getCellValue(row.getCell(idx.city));
        String district = getCellValue(row.getCell(idx.district));
        String addressDetail = getCellValue(row.getCell(idx.addressDetail));
        String birthdayStr = getCellValue(row.getCell(idx.birthday));
        String notes = getCellValue(row.getCell(idx.notes));
        String groupName = getCellValue(row.getCell(idx.group));

        // Parse birthday
        LocalDate birthday = null;
        if (StringUtils.hasText(birthdayStr)) {
            birthday = parseBirthday(birthdayStr);
        }

        // Handle group: find existing or create new under current user
        ContactGroup group = resolveGroup(groupName, user, groupRepository);

        // Resolve contact by UID if provided; otherwise create new
        Contact contact;
        boolean isNew = true;
        if (StringUtils.hasText(uid)) {
            Optional<Contact> existing = contactRepository.findByUid(uid);
            if (existing.isPresent()) {
                Contact existingContact = existing.get();
                if (!existingContact.getUser().getId().equals(user.getId())) {
                    result.addFailure("第" + (row.getRowNum() + 1) + "行: UID 所属用户不匹配");
                    return;
                }
                contact = existingContact;
                isNew = false;
            } else {
                contact = new Contact();
                contact.setUid(uid.trim());
            }
        } else {
            contact = new Contact();
        }

        contact.setUser(user);
        contact.setGroup(group);
        contact.setFamilyName(familyName);
        contact.setGivenName(givenName);
        contact.setName(name);
        contact.setGender(gender);
        contact.setPhoneMobile(phoneMobile);
        contact.setPhoneHome(phoneHome);
        contact.setPhoneWork(phoneWork);
        contact.setEmail(email);
        contact.setCompany(company);
        contact.setJobTitle(jobTitle);
        contact.setProvince(province);
        contact.setCity(city);
        contact.setDistrict(district);
        contact.setAddressDetail(addressDetail);
        contact.setBirthday(birthday);
        contact.setNotes(notes);
        contact.setIsDeleted(false);
        if (isNew) {
            contact.setCreatedAt(LocalDateTime.now());
        }

        contactRepository.save(contact);
        result.setSuccessCount(result.getSuccessCount() + 1);
    }

    private static ContactGroup resolveGroup(String groupName, User user,
                                              ContactGroupRepository groupRepository) {
        if (!StringUtils.hasText(groupName)) {
            return groupRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .orElse(null);
        }
        return groupRepository.findByUserIdAndName(user.getId(), groupName)
                .orElseGet(() -> {
                    ContactGroup newGroup = new ContactGroup();
                    newGroup.setName(groupName);
                    newGroup.setUser(user);
                    newGroup.setIsDefault(false);
                    newGroup.setSortOrder(0);
                    return groupRepository.save(newGroup);
                });
    }

    private static String[] splitName(String name) {
        if (name.length() >= 2) {
            return new String[]{name.substring(0, 1), name.substring(1)};
        }
        return new String[]{name, ""};
    }

    private static Integer parseGender(String genderStr) {
        if (!StringUtils.hasText(genderStr)) {
            return 0;
        }
        return switch (genderStr.trim()) {
            case "男" -> 1;
            case "女" -> 2;
            case "不便透露", "未知" -> 0;
            default -> 0;
        };
    }

    private static String genderToString(Integer gender) {
        if (gender == null) {
            return "不便透露";
        }
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "不便透露";
        };
    }

    /**
     * Parse birthday string into LocalDate, trying multiple formats.
     */
    private static LocalDate parseBirthday(String value) {
        String trimmed = value.trim();
        String[] formats = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd", "yyyy-M-d", "yyyy/M/d"};
        for (String format : formats) {
            try {
                return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern(format));
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        log.warn("Unable to parse birthday: {}", value);
        return null;
    }

    /**
     * Safely get cell value as string, handling different cell types.
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        yield cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMATTER);
                    } catch (Exception e) {
                        yield String.valueOf(cell.getNumericCellValue());
                    }
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    // Try to evaluate as numeric first
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        yield cell.getStringCellValue();
                    } catch (Exception e2) {
                        yield "";
                    }
                }
            }
            default -> "";
        };
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    /**
     * Maps header column names to zero-based indices.
     * Supports both the new template and the legacy template.
     */
    private static class ColumnIndex {
        int uid = -1;
        int familyName = -1;
        int givenName = -1;
        int displayName = -1;
        int name = -1;
        int gender = -1;
        int phoneMobile = -1;
        int phoneHome = -1;
        int phoneWork = -1;
        int email = -1;
        int company = -1;
        int jobTitle = -1;
        int province = -1;
        int city = -1;
        int district = -1;
        int addressDetail = -1;
        int birthday = -1;
        int notes = -1;
        int group = -1;

        ColumnIndex(Row headerRow) {
            Map<String, Integer> indexByHeader = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = getCellValue(cell);
                if (StringUtils.hasText(header)) {
                    indexByHeader.put(normalizeHeader(header), cell.getColumnIndex());
                }
            }

            uid = indexByHeader.getOrDefault("uid", -1);
            familyName = indexByHeader.getOrDefault("姓", -1);
            givenName = indexByHeader.getOrDefault("名", -1);
            displayName = indexByHeader.getOrDefault("显示名", indexByHeader.getOrDefault("fn", -1));
            name = indexByHeader.getOrDefault("姓名", -1);
            gender = indexByHeader.getOrDefault("性别", -1);
            phoneMobile = indexByHeader.getOrDefault("手机", -1);
            phoneHome = indexByHeader.getOrDefault("家庭电话", -1);
            phoneWork = indexByHeader.getOrDefault("工作电话", -1);
            email = indexByHeader.getOrDefault("邮箱", -1);
            company = indexByHeader.getOrDefault("单位", indexByHeader.getOrDefault("公司", -1));
            jobTitle = indexByHeader.getOrDefault("职位", -1);
            province = indexByHeader.getOrDefault("省", -1);
            city = indexByHeader.getOrDefault("市", -1);
            district = indexByHeader.getOrDefault("区", -1);
            addressDetail = indexByHeader.getOrDefault("详细地址", -1);
            birthday = indexByHeader.getOrDefault("生日", -1);
            notes = indexByHeader.getOrDefault("备注", -1);
            group = indexByHeader.getOrDefault("所属分组", -1);
        }

        private static String normalizeHeader(String header) {
            return header.trim().replaceAll("\\s+", "").toLowerCase();
        }
    }
}
