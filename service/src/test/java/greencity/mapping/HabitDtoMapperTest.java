package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.habit.HabitDto;
import greencity.entity.Habit;
import greencity.entity.HabitTranslation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.Model;

import java.util.Collections;

import static greencity.ModelUtils.getHabitAssign;
import static greencity.ModelUtils.getLanguage;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitDtoMapperTest {
    @InjectMocks
    private HabitDtoMapper mapper;

    @Test
    @DisplayName("convert: Habit -> HabitDto (id, image, translations)")
    void convert_ok() {
        HabitTranslation entity = HabitTranslation.builder()
            .id(1L)
            .name("")
            .description("")
            .habitItem("")
            .language(getLanguage())
            .habit(getHabitAssign().getHabit()
                .setTags(Collections.emptySet()))
            .build();

        HabitDto expected = HabitDto.builder()
            .id(1L)
            .image("")
            .defaultDuration(null)
            .complexity(null)
            .habitTranslation(greencity.dto.habittranslation.HabitTranslationDto.builder()
                .name("")
                .description("")
                .habitItem("")
                .languageCode(getLanguage().getCode())
                .build())
            .tags(Collections.emptyList())
            .shoppingListItems(Collections.emptyList())
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        HabitTranslation entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}