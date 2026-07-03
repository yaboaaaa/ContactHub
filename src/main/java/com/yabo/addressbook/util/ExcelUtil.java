package com.yabo.addressbook.util;

import com.yabo.addressbook.entity.Contact;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);
    private static final String[] HEADERS = {
            "姓", "名", "显示名", "性别", "手机", "家庭电话", "工作电话",
            "邮箱", "单位", "职位", "省", "市", "区", "详细地址", "生日", "备注", "所属分组"
    };
    private static final int[] COLUMN_WIDTHS = {
            8, 8, 16, 8, 18, 16, 16,
            24, 20, 16, 10, 10, 10, 30, 14, 30, 16
    };
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ExcelUtil() {
    }

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

            // Set column widths
            for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
                sheet.setColumnWidth(i, COLUMN_WIDTHS[i] * 256);
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

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}