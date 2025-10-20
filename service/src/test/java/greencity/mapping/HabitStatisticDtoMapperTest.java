package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.habitstatistic.HabitStatisticDto;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatistic;
import greencity.enums.HabitRate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static greencity.ModelUtils.getHabitStatistic;
import static greencity.ModelUtils.zonedDateTime;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitStatisticDtoMapperTest {
    @InjectMocks
    private HabitStatisticDtoMapper mapper;

    @Test
    @DisplayName("convert: HabitStatistic -> HabitStatisticDto")
    void convert_ok() {
        HabitStatistic entity = getHabitStatistic()
            .setCreateDate(zonedDateTime)
            .setHabitAssign(HabitAssign.builder()
                .id(1L)
                .build());

        HabitStatisticDto expected = HabitStatisticDto.builder()
            .id(1L)
            .amountOfItems(10)
            .createDate(zonedDateTime)
            .habitRate(HabitRate.GOOD)
            .habitAssignId(1L)
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        HabitStatistic entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}