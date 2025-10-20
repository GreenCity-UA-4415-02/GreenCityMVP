package greencity.mapping;

import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.entity.CustomShoppingListItem;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class CustomShoppingListMapperTest {

    @InjectMocks
    private CustomShoppingListMapper mapper;

    @Test
    @DisplayName("Convert: DTO -> Entity maps text and status")
    void convert_ok() {
        CustomShoppingListItemResponseDto dto = CustomShoppingListItemResponseDto.builder()
            .id(1L)
            .text("text")
            .status(ShoppingListItemStatus.ACTIVE)
            .build();

        CustomShoppingListItem expected = CustomShoppingListItem.builder()
            .id(1L)
            .text("text")
            .status(ShoppingListItemStatus.ACTIVE)
            .build();

        assertEquals(expected, mapper.convert(dto));
    }

    @Test
    @DisplayName("Convert: List of DTO -> List of entity")
    void convertAllToList_ok() {
        List<CustomShoppingListItemResponseDto> dtoList = List.of(CustomShoppingListItemResponseDto.builder()
            .id(1L)
            .text("text")
            .status(ShoppingListItemStatus.ACTIVE)
            .build());

        List<CustomShoppingListItem> expected = List.of(CustomShoppingListItem.builder()
            .id(1L)
            .text("text")
            .status(ShoppingListItemStatus.ACTIVE)
            .build());

        assertEquals(expected, mapper.mapAllToList(dtoList));
    }

    @Test
    @DisplayName("Convert: null -> NPE")
    void convert_null() {
        CustomShoppingListItemResponseDto dto = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(dto));
    }

    @Test
    @DisplayName("Convert: null list -> NPE")
    void convertAllToList_null() {
        List<CustomShoppingListItemResponseDto> list = null;

        assertThrows(NullPointerException.class, () -> mapper.mapAllToList(list));
    }
}