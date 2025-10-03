package greencity.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ImageValidatorTest {
    private ImageValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ImageValidator();
        validator.initialize(null);
    }

    @Test
    @DisplayName("isValid(null) → true (image is optional)")
    void nullFile_true() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @ParameterizedTest(name = "Valid contentType={0} → true")
    @ValueSource(strings = {"image/png", "image/jpeg", "image/jpg"})
    void validTypes_true(String contentType) {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(contentType);

        boolean result = validator.isValid(file, null);

        assertThat(result).isTrue();
    }

    @ParameterizedTest(name = "Invalid contentType=={0} → false")
    @ValueSource(strings = {"image/gif", "application/pdf", "text/plain", "application/octet-stream"})
    void invalidTypes_false(String contentType) {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(contentType);

        boolean result = validator.isValid(file, null);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Boundary: contentType == null → false")
    void nullContentType_false() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getContentType()).thenReturn(null);

        boolean result = validator.isValid(file, null);

        assertThat(result).isFalse();
    }
}