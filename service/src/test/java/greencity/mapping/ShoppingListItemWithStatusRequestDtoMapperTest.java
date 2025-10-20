package greencity.mapping;

import greencity.dto.shoppinglistitem.ShoppingListItemWithStatusRequestDto;
import greencity.entity.ShoppingListItem;
import greencity.entity.UserShoppingListItem;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
class ShoppingListItemWithStatusRequestDtoMapperTest {
    @InjectMocks
    private ShoppingListItemWithStatusRequestDtoMapper mapper;

    @Test
    @DisplayName("convert: ShoppingListItemWithStatusRequestDto -> UserShoppingListItem (custom status)")
    void convert_ok() {
        ShoppingListItemWithStatusRequestDto dto =
            ShoppingListItemWithStatusRequestDto.builder()
                .id(7L)
                .status(ShoppingListItemStatus.INPROGRESS)
                .build();

        UserShoppingListItem expected = UserShoppingListItem.builder()
            .shoppingListItem(ShoppingListItem.builder().id(7L).build())
            .status(ShoppingListItemStatus.INPROGRESS)
            .build();

        assertEquals(expected, mapper.convert(dto));
    }

    @Test
    void convert_null() {
        ShoppingListItemWithStatusRequestDto dto = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(dto));
    }
}