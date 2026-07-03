package com.example.addressbook.util;

import com.example.addressbook.dto.ImportResult;
import com.example.addressbook.entity.Contact;
import com.example.addressbook.entity.ContactGroup;
import com.example.addressbook.entity.User;
import com.example.addressbook.repository.ContactGroupRepository;
import com.example.addressbook.repository.ContactRepository;
import com.example.addressbook.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
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
import org.springframework.data.jpa.domain.Specification;
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
import java.util.List;

public class ExcelUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);
    private static final int MAX_ROWS = 5000;
    private static final String[] HEADERS = {
            "姓名", "性别", "手机", "家庭电话", "工作电话", "邮箱", "单位", "职位",
            "省", "市", "区", "详细地址", "生日", "备注", "所属分组"
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

                row.createCell(0).setCellValue(nullToEmpty(contact.getName()));

                // Gender: 0->"未知", 1->"男", 2->"女"
                String genderStr = "未知";
                if (contact.getGender() != null) {
                    genderStr = switch (contact.getGender()) {
                        case 1 -> "男";
                        case 2 -> "女";
                        default -> "未知";
                    };
                }
                row.createCell(1).setCellValue(genderStr);
                row.createCell(2).setCellValue(nullToEmpty(contact.getPhoneMobile()));
                row.createCell(3).setCellValue(nullToEmpty(contact.getPhoneHome()));
                row.createCell(4).setCellValue(nullToEmpty(contact.getPhoneWork()));
                row.createCell(5).setCellValue(nullToEmpty(contact.getEmail()));
                row.createCell(6).setCellValue(nullToEmpty(contact.getCompany()));
                row.createCell(7).setCellValue(nullToEmpty(contact.getJobTitle()));
                row.createCell(8).setCellValue(nullToEmpty(contact.getProvince()));
                row.createCell(9).setCellValue(nullToEmpty(contact.getCity()));
                row.createCell(10).setCellValue(nullToEmpty(contact.getDistrict()));
                row.createCell(11).setCellValue(nullToEmpty(contact.getAddressDetail()));
                row.createCell(12).setCellValue(contact.getBirthday() != null ? contact.getBirthday().format(DATE_FORMATTER) : "");
                row.createCell(13).setCellValue(nullToEmpty(contact.getNotes()));
                row.createCell(14).setCellValue(contact.getGroup() != null ? nullToEmpty(contact.getGroup().getName()) : "");
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

            // Start from row 1 (skip header row)
            for (int i = 1; i <= rowsToProcess; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    processRow(row, user, groupRepository, contactRepository, result);
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
    private static void processRow(Row row, User user,
                                    ContactGroupRepository groupRepository,
                                    ContactRepository contactRepository,
                                    ImportResult result) {
        String name = getCellValue(row.getCell(0));
        if (!StringUtils.hasText(name)) {
            result.addFailure("第" + (row.getRowNum() + 1) + "行: 姓名为空，已跳过");
            return;
        }

        // Parse gender
        String genderStr = getCellValue(row.getCell(1));
        Integer gender = switch (genderStr) {
            case "男" -> 1;
            case "女" -> 2;
            default -> 0;
        };

        String phoneMobile = getCellValue(row.getCell(2));
        String phoneHome = getCellValue(row.getCell(3));
        String phoneWork = getCellValue(row.getCell(4));

        // Validate: at least one phone number required
        if (!StringUtils.hasText(phoneMobile) && !StringUtils.hasText(phoneHome) && !StringUtils.hasText(phoneWork)) {
            result.addFailure("第" + (row.getRowNum() + 1) + "行: " + name + " 至少需要一个联系电话");
            return;
        }

        // Check duplicate by name + phoneMobile
        if (StringUtils.hasText(phoneMobile)) {
            boolean exists = checkDuplicate(name, phoneMobile, user.getId(), contactRepository);
            if (exists) {
                result.addFailure("第" + (row.getRowNum() + 1) + "行: " + name + "(" + phoneMobile + ") 已存在");
                return;
            }
        }

        String email = getCellValue(row.getCell(5));
        String company = getCellValue(row.getCell(6));
        String jobTitle = getCellValue(row.getCell(7));
        String province = getCellValue(row.getCell(8));
        String city = getCellValue(row.getCell(9));
        String district = getCellValue(row.getCell(10));
        String addressDetail = getCellValue(row.getCell(11));
        String birthdayStr = getCellValue(row.getCell(12));
        String notes = getCellValue(row.getCell(13));
        String groupName = getCellValue(row.getCell(14));

        // Parse birthday
        LocalDate birthday = null;
        if (StringUtils.hasText(birthdayStr)) {
            birthday = parseBirthday(birthdayStr);
        }

        // Handle group: find existing or create new
        ContactGroup group = null;
        if (StringUtils.hasText(groupName)) {
            group = groupRepository.findByUserIdAndName(user.getId(), groupName)
                    .orElseGet(() -> {
                        ContactGroup newGroup = new ContactGroup();
                        newGroup.setName(groupName);
                        newGroup.setUser(user);
                        newGroup.setIsDefault(false);
                        newGroup.setSortOrder(0);
                        return groupRepository.save(newGroup);
                    });
        } else {
            group = groupRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .orElse(null);
        }

        // Create contact entity
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setGroup(group);
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
        contact.setCreatedAt(LocalDateTime.now());

        contactRepository.save(contact);
        result.setSuccessCount(result.getSuccessCount() + 1);
    }

    /**
     * Check if a contact with the same name and phoneMobile already exists for the user.
     */
    private static boolean checkDuplicate(String name, String phoneMobile, Long userId,
                                           ContactRepository contactRepository) {
        Specification<Contact> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.equal(root.get("isDeleted"), false));
            predicates.add(cb.equal(root.get("name"), name));
            predicates.add(cb.equal(root.get("phoneMobile"), phoneMobile));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return contactRepository.count(spec) > 0;
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
}