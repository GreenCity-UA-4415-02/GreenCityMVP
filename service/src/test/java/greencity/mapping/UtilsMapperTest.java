package greencity.mapping;

import greencity.constant.AppConstant;
import greencity.dto.language.LanguageVO;
import greencity.entity.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

import static greencity.ModelUtils.getLanguage;
import static greencity.ModelUtils.getLanguageUa;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class UtilsMapperTest {

    @Test
    @DisplayName("map: Language -> LanguageVO")
    void map_single_ok() {
        Language entity = getLanguage();
        LanguageVO expected = LanguageVO.builder()
                .id(1L)
                .code(AppConstant.DEFAULT_LANGUAGE_CODE)
                .build();

        assertEquals(expected, UtilsMapper.map(entity, LanguageVO.class));
    }

    @Test
    @DisplayName("mapAllToList: List<Language> -> List<LanguageVO>")
    void mapAllToList_ok() {
        List<Language> list = List.of(getLanguage(), getLanguageUa());
        List<LanguageVO> expected = List.of(LanguageVO.builder()
                        .id(1L)
                        .code(AppConstant.DEFAULT_LANGUAGE_CODE)
                        .build(),
                LanguageVO.builder()
                        .id(2L)
                        .code("ua")
                        .build());

        assertEquals(expected, UtilsMapper.mapAllToList(list, LanguageVO.class));
    }

    @Test
    @DisplayName("mapAllToSet: List<Language> -> Set<LanguageVO> (унікальність збережена)")
    void mapAllToSet_ok() {
        List<Language> list = List.of(getLanguage(), getLanguageUa());
        Set<LanguageVO> expected = Set.of(LanguageVO.builder()
                        .id(1L)
                        .code(AppConstant.DEFAULT_LANGUAGE_CODE)
                        .build(),
                LanguageVO.builder()
                        .id(2L)
                        .code("ua")
                        .build());

        assertEquals(expected, UtilsMapper.mapAllToSet(list, LanguageVO.class));
    }

    @Test
    @DisplayName("map: null -> кидає IllegalArgumentException (STRICT, без null-handling)")
    void map_null_throws() {
        Language entity = null;

        assertThrows(IllegalArgumentException.class, () -> UtilsMapper.map(entity, LanguageVO.class));
    }
}