package greencity.mapping;

import greencity.dto.category.CategoryDto;
import greencity.dto.category.CategoryDtoResponse;
import greencity.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class CategoryDtoResponseMapperTest {

    @InjectMocks
    private CategoryDtoResponseMapper mapper;

    @Test
    @DisplayName("Expected mapping OK")
    void convertTest() {
        Category entity = Category.builder()
                .id(1L)
                .name("name")
                .build();

        CategoryDtoResponse expected = CategoryDtoResponse.builder()
                .id(1L)
                .name("name")
                .build();

        CategoryDtoResponse actual = mapper.convert(entity);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Convert: null source -> throws NPE")
    void convert_null_throwsNpe() {
        Category entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }

    @Test
    @DisplayName("Convert: empty name -> maps empty string")
    void convert_emptyName_ok() {
        Category entity = Category.builder()
                .id(1L)
                .name("")
                .build();

        CategoryDtoResponse dto = mapper.convert(entity);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("", dto.getName());
    }

    @Test
    @DisplayName("Convert: null name in entity -> maps null to dto.name")
    void convert_nullName_ok() {
        Category entity = Category.builder()
                .id(1L)
                .name(null)
                .build();

        CategoryDtoResponse dto = mapper.convert(entity);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNull(dto.getName());
    }
}