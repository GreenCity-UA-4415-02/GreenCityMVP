package greencity.mapping;

import greencity.ModelUtils;
import greencity.TestConst;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.entity.User; // автор, якщо так
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class EcoNewsAuthorDtoMapperTest {

    @InjectMocks
    private EcoNewsAuthorDtoMapper mapper;

    @Test
    @DisplayName("Convert: User -> EcoNewsAuthorDto")
    void convert_ok() {
        User user = ModelUtils.getUser();

        EcoNewsAuthorDto expected = EcoNewsAuthorDto.builder()
            .id(1L)
            .name(TestConst.NAME)
            .build();

        assertEquals(expected, mapper.convert(user));
    }

    @Test
    @DisplayName("Convert: null -> NPE")
    void convert_null() {
        User user = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(user));
    }
}