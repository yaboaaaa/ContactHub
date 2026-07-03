package com.example.addressbook.util;

import com.example.addressbook.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUtilTest {

    /**
     * Create a valid PNG image as byte array.
     */
    private byte[] createValidPngImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    /**
     * Create a valid JPEG image as byte array.
     */
    private byte[] createValidJpegImage(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    @Test
    void resizeAvatar_shouldValidateJpeg() throws IOException {
        byte[] imageBytes = createValidJpegImage(400, 300);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn((long) imageBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(imageBytes));

        byte[] result = ImageUtil.resizeAvatar(file);

        assertThat(result).isNotEmpty();
        // JPEG output after resize should be less than original
        assertThat(result.length).isLessThan(imageBytes.length);
    }

    @Test
    void resizeAvatar_shouldValidatePng() throws IOException {
        byte[] imageBytes = createValidPngImage(300, 300);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn((long) imageBytes.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(imageBytes));

        byte[] result = ImageUtil.resizeAvatar(file);

        assertThat(result).isNotEmpty();
    }

    @Test
    void resizeAvatar_shouldRejectNonImageFiles() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("text/plain");

        assertThatThrownBy(() -> ImageUtil.resizeAvatar(file))
                .isInstanceOf(BusinessException.class)
                .hasMessage("仅支持 JPG 和 PNG 格式的图片");
    }

    @Test
    void resizeAvatar_shouldRejectFilesOver2MB() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(3 * 1024 * 1024L); // 3MB

        assertThatThrownBy(() -> ImageUtil.resizeAvatar(file))
                .isInstanceOf(BusinessException.class)
                .hasMessage("图片大小不能超过 2MB");
    }

    @Test
    void resizeAvatar_shouldRejectNullContentType() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(null);

        assertThatThrownBy(() -> ImageUtil.resizeAvatar(file))
                .isInstanceOf(BusinessException.class)
                .hasMessage("仅支持 JPG 和 PNG 格式的图片");
    }

    @Test
    void getContentType_shouldReturnJpegForJpgExtension() {
        assertThat(ImageUtil.getContentType("photo.jpg")).isEqualTo("image/jpeg");
        assertThat(ImageUtil.getContentType("photo.jpeg")).isEqualTo("image/jpeg");
    }

    @Test
    void getContentType_shouldReturnPngForPngExtension() {
        assertThat(ImageUtil.getContentType("photo.png")).isEqualTo("image/png");
    }

    @Test
    void getContentType_shouldReturnPngAsDefault() {
        assertThat(ImageUtil.getContentType("photo.gif")).isEqualTo("image/png");
        assertThat(ImageUtil.getContentType("photo")).isEqualTo("image/png");
    }

    @Test
    void getContentType_shouldReturnPngForNullFilename() {
        assertThat(ImageUtil.getContentType(null)).isEqualTo("image/png");
    }

    @Test
    void getContentType_shouldBeCaseInsensitive() {
        assertThat(ImageUtil.getContentType("photo.JPG")).isEqualTo("image/jpeg");
        assertThat(ImageUtil.getContentType("photo.PNG")).isEqualTo("image/png");
    }
}