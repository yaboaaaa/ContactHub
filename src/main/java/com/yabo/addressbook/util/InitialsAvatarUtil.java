package com.yabo.addressbook.util;

import com.yabo.addressbook.entity.Contact;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility for generating circular avatar images with contact initials.
 */
public class InitialsAvatarUtil {

    private static final int SIZE = 100;
    private static final String FONT_NAME = "SansSerif";
    private static final int FONT_SIZE = 36;

    private InitialsAvatarUtil() {
        // utility class
    }

    /**
     * Generate a 100x100 PNG circular avatar for the given contact.
     *
     * @param contact the contact (must not be null)
     * @return PNG image bytes
     */
    public static byte[] generateAvatar(Contact contact) {
        String initials = extractInitials(contact);
        return generateAvatar(initials, avatarColorForContact(contact));
    }

    /**
     * Generate a 100x100 PNG circular avatar with the given initials and background color.
     *
     * @param initials the text to draw
     * @param bgColor  the background color
     * @return PNG image bytes
     */
    public static byte[] generateAvatar(String initials, Color bgColor) {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // Transparent background
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, SIZE, SIZE);

        // Circular background
        g2d.setColor(bgColor);
        g2d.fill(new Ellipse2D.Double(0, 0, SIZE, SIZE));

        // Draw initials centered
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(FONT_NAME, Font.BOLD, FONT_SIZE));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (SIZE - fm.stringWidth(initials)) / 2;
        int textY = ((SIZE - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(initials, textX, textY);

        g2d.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成头像失败", e);
        }
    }

    /**
     * Extract up to two initials from a contact's name.
     * Prefers familyName + givenName; falls back to display name.
     */
    public static String extractInitials(Contact contact) {
        if (contact == null) {
            return "?";
        }

        String familyName = contact.getFamilyName();
        String givenName = contact.getGivenName();
        if (StringUtils.hasText(familyName) && StringUtils.hasText(givenName)) {
            return firstChar(familyName) + firstChar(givenName);
        }

        String displayName = contact.getDisplayName();
        if (!StringUtils.hasText(displayName)) {
            return "?";
        }
        String trimmed = displayName.trim();
        if (trimmed.length() >= 2) {
            return firstChar(trimmed) + firstChar(trimmed.substring(1));
        }
        return firstChar(trimmed);
    }

    /**
     * Return a deterministic background color based on the contact id/display name.
     */
    public static Color avatarColorForContact(Contact contact) {
        if (contact == null || contact.getId() == null) {
            return new Color(100, 149, 237); // cornflower blue
        }
        return avatarColorForSeed(contact.getId().hashCode());
    }

    /**
     * Pick a pleasing color from a predefined palette based on a seed value.
     */
    public static Color avatarColorForSeed(int seed) {
        Color[] palette = {
                new Color(239, 83, 80),   // red
                new Color(38, 166, 154),  // teal
                new Color(66, 165, 245),  // blue
                new Color(255, 167, 38),  // orange
                new Color(126, 87, 194),  // deep purple
                new Color(236, 64, 122),  // pink
                new Color(42, 157, 143),  // green
                new Color(233, 196, 106), // yellow
                new Color(90, 106, 207),  // indigo
                new Color(250, 130, 49)   // coral
        };
        return palette[Math.abs(seed) % palette.length];
    }

    private static String firstChar(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().substring(0, 1).toUpperCase();
    }
}
