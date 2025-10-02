package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.language.LanguageVO;
import greencity.dto.tag.TagTranslationVO;
import greencity.dto.tag.TagVO;
import greencity.entity.Language;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class TagVOMapperTest {
    @InjectMocks
    private TagVOMapper mapper;

    @Test
    @DisplayName("convert: Tag -> TagVO")
    void convert_ok_withTranslations() {
        Tag entity = ModelUtils.getTag().setTagTranslations(
                Arrays.asList(TagTranslation.builder().id(1L).name("Новини")
                                .language(Language.builder().id(1L).code("ua").build()).build(),
                        TagTranslation.builder().id(2L).name("News").language(Language.builder().id(2L).code("en").build())
                                .build()));

        TagVO expected = ModelUtils.getTagVO().setTagTranslations(
                Arrays.asList(TagTranslationVO.builder().id(1L).name("Новини")
                                .languageVO(LanguageVO.builder().id(1L).code("ua").build()).build(),
                        TagTranslationVO.builder().id(2L).name("News").languageVO(LanguageVO.builder().id(2L).code("en").build())
                                .build()));

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("convert: Tag null -> NPE")
    void convert_nullTag_npe() {
        Tag entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}