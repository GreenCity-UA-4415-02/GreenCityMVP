package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.tag.NewTagDto;
import greencity.entity.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class NewTagDtoMapperTest {
    @InjectMocks
    private NewTagDtoMapper mapper;

    @Test
    @DisplayName("convert: Tag -> NewTagDto (ua/en)")
    void convert_ok() {
        Tag entity = ModelUtils.getTag();

        NewTagDto expected = NewTagDto.builder()
                .id(1L)
                .name("News")
                .nameUa("Новини")
                .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        Tag entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}