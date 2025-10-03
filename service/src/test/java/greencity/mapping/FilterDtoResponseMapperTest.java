package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.user.UserFilterDtoResponse;
import greencity.entity.Filter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class FilterDtoResponseMapperTest {

    @InjectMocks
    private FilterDtoResponseMapper mapper;

    @Test
    @DisplayName("convert: Entity -> Response DTO")
    void convert_ok() {
        Filter entity = ModelUtils.getFilter();
        UserFilterDtoResponse expected = ModelUtils.getUserFilterDtoResponse();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("convert: null -> NPE")
    void convert_null() {
        Filter entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}