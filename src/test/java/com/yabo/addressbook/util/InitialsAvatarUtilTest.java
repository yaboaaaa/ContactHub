package com.yabo.addressbook.util;

import com.yabo.addressbook.entity.Contact;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.assertj.core.api.Assertions.assertThat;

class InitialsAvatarUtilTest {

    @Test
    void extractInitials_shouldUseFamilyAndGivenName() {
        Contact c = new Contact();
        c.setFamilyName("张");
        c.setGivenName("三");
        assertThat(InitialsAvatarUtil.extractInitials(c)).isEqualTo("张三");
    }

    @Test
    void extractInitials_shouldUseFamilyOnly_whenNoGivenName() {
        Contact c = new Contact();
        c.setFamilyName("李");
        assertThat(InitialsAvatarUtil.extractInitials(c)).isEqualTo("李");
    }

    @Test
    void extractInitials_shouldReturnSingleCharForShortName() {
        Contact c = new Contact();
        c.setFamilyName("A");
        assertThat(InitialsAvatarUtil.extractInitials(c)).isEqualTo("A");
    }

    @Test
    void extractInitials_shouldReturnQuestionMarkForNull() {
        assertThat(InitialsAvatarUtil.extractInitials(null)).isEqualTo("?");
    }

    @Test
    void extractInitials_shouldReturnQuestionMarkForEmptyName() {
        Contact c = new Contact();
        assertThat(InitialsAvatarUtil.extractInitials(c)).isEqualTo("?");
    }

    @Test
    void avatarColorForContact_shouldReturnDeterministicColor() {
        Contact c = new Contact();
        c.setId(1L);
        Color color1 = InitialsAvatarUtil.avatarColorForContact(c);
        Color color2 = InitialsAvatarUtil.avatarColorForContact(c);
        assertThat(color1).isEqualTo(color2);
    }

    @Test
    void avatarColorForContact_shouldReturnDefaultForNullId() {
        Contact c = new Contact();
        Color color = InitialsAvatarUtil.avatarColorForContact(c);
        assertThat(color).isEqualTo(new Color(100, 149, 237));
    }

    @Test
    void avatarColorForContact_shouldReturnDefaultForNullContact() {
        assertThat(InitialsAvatarUtil.avatarColorForContact(null)).isEqualTo(new Color(100, 149, 237));
    }

    @Test
    void avatarColorForSeed_shouldReturnValidPaletteColor() {
        for (int i = 0; i < 20; i++) {
            Color c = InitialsAvatarUtil.avatarColorForSeed(i);
            assertThat(c).isNotNull();
        }
    }

    @Test
    void generateAvatar_shouldReturnValidPngBytes() {
        byte[] bytes = InitialsAvatarUtil.generateAvatar("AB", new Color(255, 0, 0));
        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(0);
        // PNG magic bytes
        assertThat(bytes[0]).isEqualTo((byte) 0x89);
        assertThat(bytes[1]).isEqualTo((byte) 0x50);
        assertThat(bytes[2]).isEqualTo((byte) 0x4E);
        assertThat(bytes[3]).isEqualTo((byte) 0x47);
    }

    @Test
    void generateAvatar_fromContact_shouldReturnValidPngBytes() {
        Contact c = new Contact();
        c.setId(1L);
        c.setFamilyName("张");
        c.setGivenName("三");
        byte[] bytes = InitialsAvatarUtil.generateAvatar(c);
        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(100);
    }
}
