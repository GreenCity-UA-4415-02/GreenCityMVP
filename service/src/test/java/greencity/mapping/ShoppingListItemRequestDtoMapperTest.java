package greencity.mapping;

import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
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
class ShoppingListItemRequestDtoMapperTest {
    @InjectMocks
    private ShoppingListItemRequestDtoMapper mapper;

    @Test
    @DisplayName("convert: ShoppingListItemRequestDto -> UserShoppingListItem (ACTIVE)")
    void convert_ok() {
        ShoppingListItemRequestDto dto = ShoppingListItemRequestDto.builder()
                .id(1L)
                .build();

        UserShoppingListItem expected = UserShoppingListItem.builder()
                .shoppingListItem(ShoppingListItem.builder().id(1L).build())
                .status(ShoppingListItemStatus.ACTIVE)
                .build();

        assertEquals(expected, mapper.convert(dto));
    }

    @Test
    void convert_null() {
        ShoppingListItemRequestDto dto = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(dto));
    }
}