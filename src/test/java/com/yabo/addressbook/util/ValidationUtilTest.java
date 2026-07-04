package com.yabo.addressbook.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void testIsValidEmail() {
        assertTrue(ValidationUtil.isValidEmail(null));
        assertTrue(ValidationUtil.isValidEmail(""));
        assertTrue(ValidationUtil.isValidEmail("  "));
        assertTrue(ValidationUtil.isValidEmail("test@example.com"));
        assertTrue(ValidationUtil.isValidEmail("user.name@domain.co.uk"));
        assertTrue(ValidationUtil.isValidEmail("user+tag@example.com"));

        assertFalse(ValidationUtil.isValidEmail("invalid"));
        assertFalse(ValidationUtil.isValidEmail("invalid@"));
        assertFalse(ValidationUtil.isValidEmail("@invalid.com"));
        assertFalse(ValidationUtil.isValidEmail("invalid@.com"));
        assertFalse(ValidationUtil.isValidEmail("invalid@com"));
    }

    @Test
    void testIsValidUsername() {
        assertTrue(ValidationUtil.isValidUsername("abc"));
        assertTrue(ValidationUtil.isValidUsername("user123"));
        assertTrue(ValidationUtil.isValidUsername("user_name"));
        assertTrue(ValidationUtil.isValidUsername("UserName123"));

        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername(""));
        assertFalse(ValidationUtil.isValidUsername("  "));
        assertFalse(ValidationUtil.isValidUsername("ab"));
        assertFalse(ValidationUtil.isValidUsername("a".repeat(21)));
        assertFalse(ValidationUtil.isValidUsername("user-name"));
        assertFalse(ValidationUtil.isValidUsername("user name"));
        assertFalse(ValidationUtil.isValidUsername("user@name"));
    }

    @Test
    void testIsValidPassword() {
        assertTrue(ValidationUtil.isValidPassword("123456"));
        assertTrue(ValidationUtil.isValidPassword("password"));
        assertTrue(ValidationUtil.isValidPassword("1234567890"));

        assertFalse(ValidationUtil.isValidPassword(null));
        assertFalse(ValidationUtil.isValidPassword(""));
        assertFalse(ValidationUtil.isValidPassword("12345"));
    }

    @Test
    void testValidateEmail() {
        assertNull(ValidationUtil.validateEmail(null));
        assertNull(ValidationUtil.validateEmail(""));
        assertNull(ValidationUtil.validateEmail("  "));
        assertNull(ValidationUtil.validateEmail("test@example.com"));
        assertNull(ValidationUtil.validateEmail("  test@example.com  "));

        assertNotNull(ValidationUtil.validateEmail("invalid"));
        assertNotNull(ValidationUtil.validateEmail("invalid@"));
        assertNotNull(ValidationUtil.validateEmail("@invalid.com"));
    }

    @Test
    void testValidateUsername() {
        assertEquals("用户名不能为空", ValidationUtil.validateUsername(null));
        assertEquals("用户名不能为空", ValidationUtil.validateUsername(""));
        assertEquals("用户名不能为空", ValidationUtil.validateUsername("  "));

        assertEquals("用户名只能包含字母、数字和下划线，长度3-20位", ValidationUtil.validateUsername("ab"));
        assertEquals("用户名只能包含字母、数字和下划线，长度3-20位", ValidationUtil.validateUsername("a".repeat(21)));
        assertEquals("用户名只能包含字母、数字和下划线，长度3-20位", ValidationUtil.validateUsername("user-name"));

        assertNull(ValidationUtil.validateUsername("abc"));
        assertNull(ValidationUtil.validateUsername("user123"));
        assertNull(ValidationUtil.validateUsername("user_name"));
    }

    @Test
    void testValidatePassword() {
        assertEquals("密码不能为空", ValidationUtil.validatePassword(null));
        assertEquals("密码不能为空", ValidationUtil.validatePassword(""));
        assertEquals("密码至少6位", ValidationUtil.validatePassword("12345"));

        assertNull(ValidationUtil.validatePassword("123456"));
        assertNull(ValidationUtil.validatePassword("password"));
    }
}
