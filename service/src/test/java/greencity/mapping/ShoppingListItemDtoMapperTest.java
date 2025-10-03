package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.entity.localization.ShoppingListItemTranslation;
import greencity.enums.ShoppingListItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
class ShoppingListItemDtoMapperTest {
    private final ShoppingListItemDtoMapper mapper = new ShoppingListItemDtoMapper();

    @Test
    @DisplayName("convert: ShoppingListItemTranslation -> ShoppingListItemDto")
    void convert_ok() {
        ShoppingListItemTranslation entity = ModelUtils.getShoppingListItemTranslations().get(0);

        ShoppingListItemDto expected = new ShoppingListItemDto(
                1L,
                "Buy a bamboo toothbrush",
                ShoppingListItemStatus.ACTIVE.toString()
        );

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        ShoppingListItemTranslation entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}