package greencity.mapping;

import greencity.ModelUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
class MultipartBase64ImageMapperTest {
    @InjectMocks
    private MultipartBase64ImageMapper mapper;

    private String toDataUrl(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
        String contentType = file.getContentType();
        return "data:" + contentType + ";base64," + base64;
    }

    @BeforeEach
    void disableImageCache() {
        ImageIO.setUseCache(false);
    }

    @Test
    @DisplayName("convert: base64 PNG -> MultipartFile OK")
    void convert_ok(@TempDir Path tempDir) throws IOException {
        String oldUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());

        String img = toDataUrl(ModelUtils.getFile());

        try {
            MultipartFile result = mapper.convert(img);

            assertNotNull(result, "MultipartFile має бути створений");
            assertTrue(result.getSize() > 0, "Файл не має бути порожнім");
            assertEquals("tempImage.jpg", result.getOriginalFilename(), "Очікувана назва тимчасового файлу");
        } finally {
            System.setProperty("user.dir", oldUserDir);
        }
    }

    @Test
    @DisplayName("convert: invalid base64 -> throws IllegalStateException (decodeBase64)")
    void convert_invalid() {
        assertThrows(IllegalStateException.class,
            () -> mapper.convert("data:image/png;base64,NOT_BASE64"));
    }
}