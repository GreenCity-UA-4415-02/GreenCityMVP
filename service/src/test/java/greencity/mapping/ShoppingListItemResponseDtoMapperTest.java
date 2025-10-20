package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.shoppinglistitem.ShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.ShoppingListItemTranslationDTO;
import greencity.entity.ShoppingListItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
class ShoppingListItemResponseDtoMapperTest {
    @InjectMocks
    private ShoppingListItemResponseDtoMapper mapper;

    @Test
    @DisplayName("convert: ShoppingListItem -> ShoppingListItemResponseDto (id + translations)")
    void convert_ok() {
        ShoppingListItem entity = ModelUtils.getShoppingListItem();

        ShoppingListItemResponseDto expected = ShoppingListItemResponseDto.builder()
            .id(1L)
            .translations(List.of(
                ShoppingListItemTranslationDTO.builder()
                    .id(2L)
                    .content("Buy a bamboo toothbrush")
                    .build(),
                ShoppingListItemTranslationDTO.builder()
                    .id(11L)
                    .content("Start recycling batteries")
                    .build()))
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        ShoppingListItem entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}