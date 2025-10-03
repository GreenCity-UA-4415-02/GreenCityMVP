package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitTranslation;
import greencity.enums.HabitAssignStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static greencity.ModelUtils.localDateTime;
import static greencity.ModelUtils.zonedDateTime;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitAssignDtoMapperTest {
    @InjectMocks
    private HabitAssignDtoMapper mapper;

    @Test
    @DisplayName("convert: HabitAssign -> HabitAssignDto (basic fields)")
    void convert_ok() {
        HabitAssign entity = ModelUtils.getHabitAssign()
                .setCreateDate(zonedDateTime)
                .setLastEnrollmentDate(zonedDateTime);

        HabitAssignDto expected = HabitAssignDto.builder()
                .id(1L)
                .status(HabitAssignStatus.ACQUIRED)
                .createDateTime(zonedDateTime)
                .userId(1L)
                .duration(0)
                .habitStreak(0)
                .workingDays(0)
                .lastEnrollmentDate(zonedDateTime)
                .habitStatusCalendarDtoList(List.of(HabitStatusCalendarDto.builder()
                                .id(1L)
                                .enrollDate(LocalDate.now())
                                .build()))
                .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        HabitAssign entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}