package greencity.validator;

import greencity.service.LanguageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LanguageValidatorTest {

    private LanguageValidator languageValidator;
    private LanguageService languageServiceMock;

    @BeforeEach
    void setUp() throws Exception {
        languageValidator = new LanguageValidator();

        languageServiceMock = Mockito.mock(LanguageService.class);

        Field serviceField = LanguageValidator.class.getDeclaredField("languageService");
        serviceField.setAccessible(true);
        serviceField.set(languageValidator, languageServiceMock);

        List<String> codes = Arrays.asList("en", "ua");
        Mockito.when(languageServiceMock.findAllLanguageCodes()).thenReturn(codes);

        languageValidator.initialize(null);
    }

    @Test
    void isValid_ReturnsTrue_WhenLanguageSupported() {
        assertTrue(languageValidator.isValid(Locale.ENGLISH, null));
    }

    @Test
    void isValid_ReturnsTrue_WhenLanguageSupported_UA() {
        assertTrue(languageValidator.isValid(new Locale("ua"), null));
    }

    @Test
    void isValid_ReturnsFalse_WhenLanguageUnsupported() {
        assertFalse(languageValidator.isValid(Locale.FRENCH, null));
    }

    @Test
    void isValid_ReturnsFalse_WhenCodesListEmpty() throws Exception {
        Mockito.when(languageServiceMock.findAllLanguageCodes()).thenReturn(Collections.emptyList());
        languageValidator.initialize(null);
        assertFalse(languageValidator.isValid(Locale.ENGLISH, null));
    }

    @Test
    void initialize_LoadsCodesFromService() {
        assertTrue(languageValidator.isValid(Locale.ENGLISH, null));
        assertTrue(languageValidator.isValid(new Locale("ua"), null));
    }
}
