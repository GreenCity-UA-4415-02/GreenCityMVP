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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitTranslationMapperTest {
    @InjectMocks
    private HabitTranslationMapper mapper;

    @Test
    @DisplayName("convert: HabitTranslationDTO -> HabitTranslation")
    void convert_ok() {
        HabitTranslationDto entity = HabitTranslationDto.builder()
                .description("description")
                .habitItem("item")
                .name("name")
                .build();

        HabitTranslation expected = HabitTranslation.builder()
                .description("description")
                .habitItem("item")
                .name("name")
                .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        HabitTranslationDto entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }

    @Test
    @DisplayName("convert: HabitTranslation -> HabitTranslationDto")
    void convert_mapAllToList_ok() {
        List<HabitTranslationDto> entity = List.of(HabitTranslationDto.builder()
                .description("description")
                .habitItem("item")
                .name("name")
                .build());

        List<HabitTranslation> expected = List.of(HabitTranslation.builder()
                .description("description")
                .habitItem("item")
                .name("name")
                .build());

        assertEquals(expected, mapper.mapAllToList(entity));
    }

    @Test
    void convert_mapAllToList_null() {
        List<HabitTranslationDto> entity = null;

        assertThrows(NullPointerException.class, () -> mapper.mapAllToList(entity));
    }
}