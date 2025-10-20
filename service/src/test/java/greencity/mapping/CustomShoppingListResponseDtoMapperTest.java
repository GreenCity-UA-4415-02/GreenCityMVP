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
class CustomShoppingListResponseDtoMapperTest {
    @InjectMocks
    private CustomShoppingListResponseDtoMapper mapper;

    @Test
    @DisplayName("Convert: Entity -> ResponseDto")
    void convert_ok() {
        CustomShoppingListItem entity = CustomShoppingListItem.builder()
            .id(1L)
            .text("text")
            .status(ShoppingListItemStatus.ACTIVE)
            .build();

        CustomShoppingListItemResponseDto expected = CustomShoppingListItemResponseDto.builder()
            .id(1L)
            .text("text")
            .status(ShoppingListItemStatus.ACTIVE)
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("Convert: List of DTO -> List of entity")
    void convertAllToList_ok() {
        List<CustomShoppingListItem> dtoList = List.of(CustomShoppingListItem.builder()
            .id(1L)
            .text("text")
            .status(ShoppingListItemStatus.ACTIVE)
            .build());

        List<CustomShoppingListItemResponseDto> expected = List.of(CustomShoppingListItemResponseDto.builder()
            .id(1L)
            .text("text")
            .status(ShoppingListItemStatus.ACTIVE)
            .build());

        assertEquals(expected, mapper.mapAllToList(dtoList));
    }

    @Test
    @DisplayName("Convert: null -> NPE")
    void convert_null() {
        CustomShoppingListItem entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }

    @Test
    @DisplayName("Convert: null list -> NPE")
    void convertAllToList_null() {
        List<CustomShoppingListItem> list = null;

        assertThrows(NullPointerException.class, () -> mapper.mapAllToList(list));
    }
}