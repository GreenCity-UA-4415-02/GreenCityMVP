package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.habit.HabitAssignUserDurationDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.User;
import greencity.enums.HabitAssignStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
class HabitAssignUserDurationDtoMapperTest {
@InjectMocks
    private HabitAssignUserDurationDtoMapper mapper;

    @Test
    @DisplayName("convert: maps all fields from HabitAssign to DTO (happy path)")
    void convert_ok() {
        HabitAssign entity = ModelUtils.getHabitAssign();

        HabitAssignUserDurationDto expected = HabitAssignUserDurationDto.builder()
                .habitAssignId(1L)
                .userId(1L)
                .habitId(1L)
                .status(HabitAssignStatus.ACQUIRED)
                .workingDays(0)
                .duration(0)
                .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("convert: null habit -> NPE")
    void convert_nullHabit_throwsNpe() {
        HabitAssign entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}