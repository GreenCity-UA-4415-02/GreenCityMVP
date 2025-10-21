package greencity.mapping;

import greencity.dto.category.CategoryDto;
import greencity.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class CategoryDtoMapperTest {

    @InjectMocks
    private CategoryDtoMapper mapper;

    @Test
    @DisplayName("Expected mapping OK")
    void convertTest() {
        CategoryDto dto = CategoryDto.builder()
            .name("name")
            .build();
        Category expected = Category.builder()
            .name("name")
            .build();

        Category actual = mapper.convert(dto);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Convert: null source -> throws NPE")
    void convert_null_throwsNpe() {
        CategoryDto dto = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(dto));
    }

    @Test
    @DisplayName("Convert: empty name -> maps empty string")
    void convert_emptyName_ok() {
        CategoryDto dto = CategoryDto.builder()
            .name("")
            .build();

        Category entity = mapper.convert(dto);

        assertNotNull(entity);
        assertEquals("", entity.getName());
    }

    @Test
    @DisplayName("Convert: null name in DTO -> maps null to entity.name")
    void convert_nullName_ok() {
        CategoryDto dto = CategoryDto.builder()
            .name(null)
            .build();

        Category entity = mapper.convert(dto);

        assertNotNull(entity);
        assertNull(entity.getName());
    }
}