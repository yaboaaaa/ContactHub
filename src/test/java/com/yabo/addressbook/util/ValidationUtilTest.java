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
        assertTrue(ValidationUtil.isValidUsername("abcd"));
        assertTrue(ValidationUtil.isValidUsername("user123"));
        assertTrue(ValidationUtil.isValidUsername("UserName123"));
        assertTrue(ValidationUtil.isValidUsername("aBcDe12345"));

        assertFalse(ValidationUtil.isValidUsername(null));
        assertFalse(ValidationUtil.isValidUsername(""));
        assertFalse(ValidationUtil.isValidUsername("  "));
        assertFalse(ValidationUtil.isValidUsername("abc"));      // too short
        assertFalse(ValidationUtil.isValidUsername("1abc"));     // starts with digit
        assertFalse(ValidationUtil.isValidUsername("_abc"));     // starts with underscore
        assertFalse(ValidationUtil.isValidUsername("user_name")); // underscore not allowed
        assertFalse(ValidationUtil.isValidUsername("user-name")); // hyphen not allowed
        assertFalse(ValidationUtil.isValidUsername("a".repeat(21))); // too long
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

        assertNotNull(ValidationUtil.validateUsername("ab"));       // too short
        assertNotNull(ValidationUtil.validateUsername("1abc"));     // starts with digit
        assertNotNull(ValidationUtil.validateUsername("a".repeat(21))); // too long
        assertNotNull(ValidationUtil.validateUsername("user-name"));// hyphen
        assertNotNull(ValidationUtil.validateUsername("user_name"));// underscore

        assertNull(ValidationUtil.validateUsername("abcd"));
        assertNull(ValidationUtil.validateUsername("user123"));
        assertNull(ValidationUtil.validateUsername("UserName123"));
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
