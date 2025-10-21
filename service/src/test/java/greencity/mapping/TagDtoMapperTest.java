package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.tag.TagDto;
import greencity.entity.localization.TagTranslation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static greencity.ModelUtils.getTag;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class TagDtoMapperTest {
    @InjectMocks
    private TagDtoMapper mapper;

    @Test
    @DisplayName("convert: TagTranslation -> TagDto OK")
    void convert_ok() {
        TagTranslation entity = TagTranslation.builder()
            .tag(getTag())
            .name("News")
            .build();

        TagDto expected = ModelUtils.getTagDto().setId(1L);

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("convert: TagTranslation null -> NPE")
    void convert_nullTag_npe() {
        TagTranslation entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}