package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.user.UserFilterDtoRequest;
import greencity.entity.Filter;
import greencity.enums.FilterType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class FilterDtoRequestMapperTest {

    @InjectMocks
    private FilterDtoRequestMapper mapper;

    @Test
    @DisplayName("convert: Request DTO -> Entity")
    void convert_ok() {
        UserFilterDtoRequest dto = ModelUtils.getUserFilterDtoRequest();

        Filter expected = Filter.builder()
                .name("Test_Filter")
                .type(FilterType.USERS.toString())
                .values("Test;USER;ACTIVATED")
                .build();

        assertEquals(expected, mapper.convert(dto));
    }

    @Test
    @DisplayName("convert: null -> NPE")
    void convert_null() {
        UserFilterDtoRequest dto = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(dto));
    }
}