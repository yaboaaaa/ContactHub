package com.yabo.addressbook.util;

import com.yabo.addressbook.exception.BusinessException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

public class ImageUtil {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");

    private ImageUtil() {
    }

    /**
     * Validate the file extension against allowed types.
     */
    public static void validateExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        String lower = originalFilename.toLowerCase();
        boolean valid = ALLOWED_EXTENSIONS.stream().anyMatch(lower::endsWith);
        if (!valid) {
            throw new BusinessException("仅支持 JPG 和 PNG 格式的图片");
        }
    }

    /**
     * Get the output format name from the original filename.
     * Returns "jpg" for .jpg/.jpeg, "png" for .png.
     */
    public static String getOutputFormat(String originalFilename) {
        if (originalFilename == null) return "png";
        String lower = originalFilename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpg";
        return "png";
    }

    /**
     * Format file size in human-readable form (e.g., "256KB", "1.5MB").
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1fKB", bytes / 1024.0);
        } else {
            return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Validate and resize an avatar image to 200x200 center-crop, quality 0.8.
     *
     * @param file the uploaded file
     * @return resized image bytes
     * @throws BusinessException if validation fails or processing error occurs
     */
    public static byte[] resizeAvatar(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("仅支持 JPG 和 PNG 格式的图片");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("图片大小不能超过 2MB");
        }

        try {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new BusinessException("无法读取图片文件，请确认文件是否有效");
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            // Center crop to square
            int cropSize = Math.min(width, height);
            int x = (width - cropSize) / 2;
            int y = (height - cropSize) / 2;

            String format = contentType.contains("png") ? "png" : "jpg";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .sourceRegion(x, y, cropSize, cropSize)
                    .size(200, 200)
                    .outputQuality(0.8)
                    .outputFormat(format)
                    .toOutputStream(baos);

            return baos.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("图片处理失败: " + e.getMessage());
        }
    }

    /**
     * Guess content type from file extension.
     *
     * @param originalFilename the original file name
     * @return MIME type string, defaults to "image/png"
     */
    public static String getContentType(String originalFilename) {
        if (originalFilename == null) {
            return "image/png";
        }
        String lower = originalFilename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        }
        return "image/png";
    }
}