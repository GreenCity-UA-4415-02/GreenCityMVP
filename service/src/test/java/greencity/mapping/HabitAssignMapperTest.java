package greencity.mapping;

import greencity.dto.habit.HabitAssignDto;
import greencity.dto.habit.HabitAssignManagementDto;
import greencity.dto.habit.HabitDto;
import greencity.dto.user.UserShoppingListItemAdvanceDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.ShoppingListItem;
import greencity.entity.UserShoppingListItem;
import greencity.enums.HabitAssignStatus;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitAssignMapperTest {
    @InjectMocks
    private HabitAssignMapper mapper;

    @Test
    @DisplayName("Convert: HabitAssignManagementDto -> HabitAssign")
    void convert_ok() {
        HabitAssignDto dto = HabitAssignDto.builder()
            .id(1L)
            .duration(0)
            .habitStreak(0)
            .createDateTime(zonedDateTime)
            .status(HabitAssignStatus.INPROGRESS)
            .workingDays(0)
            .lastEnrollmentDate(zonedDateTime)
            .habit(HabitDto.builder()
                .id(1L)
                .complexity(0)
                .build())
            .duration(0)
            .userShoppingListItems(List.of(UserShoppingListItemAdvanceDto.builder()
                .id(1L)
                .shoppingListItemId(1L)
                .dateCompleted(localDateTime)
                .status(ShoppingListItemStatus.INPROGRESS)
                .build()))
            .build();

        HabitAssign expected = HabitAssign.builder()
            .id(1L)
            .duration(0)
            .habitStreak(0)
            .createDate(zonedDateTime)
            .status(HabitAssignStatus.INPROGRESS)
            .workingDays(0)
            .lastEnrollmentDate(zonedDateTime)
            .habit(Habit.builder()
                .id(1L)
                .complexity(0)
                .defaultDuration(0)
                .build())
            .userShoppingListItems(List.of(UserShoppingListItem.builder()
                .id(1L)
                .dateCompleted(localDateTime)
                .status(ShoppingListItemStatus.INPROGRESS)
                .shoppingListItem(ShoppingListItem.builder()
                    .id(1L)
                    .build())
                .build()))
            .build();

        assertEquals(expected, mapper.convert(dto));
    }

    @Test
    void convert_null() {
        HabitAssignDto dto = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(dto));
    }
}