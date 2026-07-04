package com.yabo.addressbook.util;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z]{4,20}$"
    );

    private ValidationUtil() {
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // email is optional
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= 6;
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null; // email is optional, null is valid
        }
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return "邮箱格式不正确";
        }
        return null; // valid
    }

    public static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        String trimmed = username.trim();
        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            return "用户名必须为英文字符，长度4-20位";
        }
        return null; // valid
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "密码不能为空";
        }
        if (password.length() < 6) {
            return "密码至少6位";
        }
        return null; // valid
    }

    public static String getTrimmedUsername(String username) {
        if (username == null) {
            return null;
        }
        return username.trim();
    }

    public static String getTrimmedEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return email.trim();
    }
}
