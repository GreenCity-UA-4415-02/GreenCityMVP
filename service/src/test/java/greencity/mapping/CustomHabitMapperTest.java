package greencity.mapping;

import greencity.ModelUtils;
import greencity.TestConst;
import greencity.entity.Habit;
import greencity.dto.habit.AddCustomHabitDtoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class CustomHabitMapperTest {

    @InjectMocks
    private CustomHabitMapper mapper;

    @Test
    @DisplayName("Convert: maps fields from DTO to Habit")
    void convert_ok() {
        AddCustomHabitDtoRequest dto = AddCustomHabitDtoRequest.builder()
            .image(TestConst.IMG_NAME)
            .complexity(1)
            .defaultDuration(2)
            .build();
        Habit expected = Habit.builder()
            .image(TestConst.IMG_NAME)
            .complexity(1)
            .defaultDuration(2)
            .isCustomHabit(true)
            .build();

        Habit actual = mapper.convert(dto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Convert: null source -> NPE")
    void convert_null_throws() {
        AddCustomHabitDtoRequest dto = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(dto));
    }
}
