package greencity.mapping;

import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatusCalendar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitStatusCalendarMapperTest {
    @InjectMocks
    private HabitStatusCalendarMapper mapper;

    @Test
    @DisplayName("convert: HabitStatusCalendarDTO -> HabitStatusCalendar")
    void convert_ok() {
        LocalDate localDate = LocalDate.now();
        HabitStatusCalendarVO entity = HabitStatusCalendarVO.builder()
            .id(1L)
            .enrollDate(localDate)
            .habitAssignVO(HabitAssignVO.builder()
                .id(1L)
                .build())
            .build();

        HabitStatusCalendar expected = HabitStatusCalendar.builder()
            .id(1L)
            .enrollDate(localDate)
            .habitAssign(HabitAssign.builder()
                .id(1L)
                .build())
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        HabitStatusCalendarVO entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}