package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.habitfact.HabitFactDtoResponse;
import greencity.dto.habitfact.HabitFactTranslationDto;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.language.LanguageDTO;
import greencity.enums.FactOfDayStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class HabitFactDtoResponseMapperTest {
    @InjectMocks
    private HabitFactDtoResponseMapper mapper;

    @Test
    @DisplayName("convert: HabitFactTranslation -> HabitFactDtoResponse")
    void convert_ok() {
        HabitFactVO entity = HabitFactVO.builder()
            .id(1L)
            .habit(null)
            .translations(List.of(ModelUtils.getFactTranslationVO()))
            .build();

        HabitFactDtoResponse expected = HabitFactDtoResponse.builder()
            .id(1L)
            .habit(null)
            .translations(List.of(
                HabitFactTranslationDto.builder()
                    .id(1L)
                    .content("Content")
                    .factOfDayStatus(FactOfDayStatus.CURRENT)
                    .language(LanguageDTO.builder()
                        .id(1L)
                        .code("en")
                        .build())
                    .build()))
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        HabitFactVO entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}