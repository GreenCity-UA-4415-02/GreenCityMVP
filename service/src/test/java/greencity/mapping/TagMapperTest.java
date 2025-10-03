package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.language.LanguageVO;
import greencity.dto.tag.TagTranslationVO;
import greencity.dto.tag.TagVO;
import greencity.entity.Language;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import greencity.enums.TagType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class TagMapperTest {
    @InjectMocks
    private TagMapper mapper;

    @Test
    @DisplayName("convert: TagVO -> Tag")
    void convert_ok_withTranslations() {
        TagVO entity = ModelUtils.getTagVO().setTagTranslations(
                Arrays.asList(TagTranslationVO.builder().id(1L).name("Новини")
                        .languageVO(LanguageVO.builder().id(1L).code("ua").build()).build(),
                TagTranslationVO.builder().id(2L).name("News").languageVO(LanguageVO.builder().id(2L).code("en").build())
                        .build()));

        Tag expected = ModelUtils.getTag().setTagTranslations(
                Arrays.asList(TagTranslation.builder().id(1L).name("Новини")
                        .language(Language.builder().id(1L).code("ua").build()).build(),
                TagTranslation.builder().id(2L).name("News").language(Language.builder().id(2L).code("en").build())
                        .build()));

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("convert: TagVO null -> NPE")
    void convert_nullTagVO_npe() {
        TagVO entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}