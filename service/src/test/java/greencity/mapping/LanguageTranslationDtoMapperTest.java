package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.language.LanguageDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.entity.HabitFactTranslation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class LanguageTranslationDtoMapperTest {
    private final LanguageTranslationDtoMapper mapper = new LanguageTranslationDtoMapper();

    @Test
    @DisplayName("convert: HabitFactTranslation -> LanguageTranslationDTO")
    void convert_ok() {
        HabitFactTranslation entity = ModelUtils.getFactTranslation();

        LanguageTranslationDTO expected = LanguageTranslationDTO.builder()
            .content("Content")
            .language(LanguageDTO.builder()
                .id(ModelUtils.getLanguage().getId())
                .code(ModelUtils.getLanguage().getCode())
                .build())
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        HabitFactTranslation entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}