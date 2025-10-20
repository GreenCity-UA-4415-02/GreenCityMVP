package greencity.mapping;

import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitTranslationDtoMapperTest {
    @InjectMocks
    private HabitTranslationDtoMapper mapper;

    @Test
    @DisplayName("convert: HabitTranslation -> HabitTranslationDto")
    void convert_ok() {
        HabitTranslation entity = HabitTranslation.builder()
            .description("description")
            .habitItem("item")
            .name("name")
            .language(Language.builder()
                .code("ua")
                .build())
            .build();

        HabitTranslationDto expected = HabitTranslationDto.builder()
            .description("description")
            .habitItem("item")
            .name("name")
            .languageCode("ua")
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        HabitTranslation entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }

    @Test
    @DisplayName("convert: HabitTranslation -> HabitTranslationDto")
    void convert_mapAllToList_ok() {
        List<HabitTranslation> entity = List.of(HabitTranslation.builder()
            .description("description")
            .habitItem("item")
            .name("name")
            .language(Language.builder()
                .code("ua")
                .build())
            .build());

        List<HabitTranslationDto> expected = List.of(HabitTranslationDto.builder()
            .description("description")
            .habitItem("item")
            .name("name")
            .languageCode("ua")
            .build());

        assertEquals(expected, mapper.mapAllToList(entity));
    }

    @Test
    void convert_mapAllToList_null() {
        List<HabitTranslation> entity = null;

        assertThrows(NullPointerException.class, () -> mapper.mapAllToList(entity));
    }
}