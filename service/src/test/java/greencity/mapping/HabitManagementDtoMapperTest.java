package greencity.mapping;

import greencity.dto.habit.HabitManagementDto;
import greencity.dto.habittranslation.HabitTranslationManagementDto;
import greencity.entity.Habit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static greencity.ModelUtils.getHabitAssign;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitManagementDtoMapperTest {
    @InjectMocks
    private HabitManagementDtoMapper mapper;

    @Test
    @DisplayName("convert: Habit -> HabitManagementDto (id + basics)")
    void convert_ok() {
        Habit entity = getHabitAssign().getHabit();

        HabitManagementDto expected = HabitManagementDto.builder()
            .id(1L)
            .image("")
            .complexity(null)
            .defaultDuration(null)
            .habitTranslations(List.of(
                HabitTranslationManagementDto.builder()
                    .id(1L)
                    .name("")
                    .description("")
                    .habitItem("")
                    .languageCode("en")
                    .build()))
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        Habit entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}