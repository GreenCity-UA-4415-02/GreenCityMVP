package greencity.mapping;

import greencity.ModelUtils;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.dto.habit.HabitAssignManagementDto;
import greencity.entity.HabitTranslation;
import greencity.enums.HabitAssignStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitAssignManagementDtoMapperTest {
    @InjectMocks
    private HabitAssignManagementDtoMapper mapper;

    @Test
    @DisplayName("convert: HabitAssign -> HabitAssignManagementDto (core fields + USLI size)")
    void convert_ok() {
        HabitAssign entity = getHabitAssign()
            .setCreateDate(zonedDateTime)
            .setLastEnrollmentDate(zonedDateTime);

        HabitAssignManagementDto expected = HabitAssignManagementDto.builder()
            .id(1L)
            .status(HabitAssignStatus.ACQUIRED)
            .createDateTime(zonedDateTime)
            .userId(1L)
            .habitId(1L)
            .duration(0)
            .habitStreak(0)
            .workingDays(0)
            .lastEnrollment(zonedDateTime)
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        HabitAssign entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}