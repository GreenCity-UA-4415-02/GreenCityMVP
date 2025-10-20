package greencity.mapping;

import greencity.ModelUtils;
import greencity.TestConst;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.entity.EcoNews;
import greencity.entity.EcoNewsComment;
import greencity.entity.Language;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.zonedDateTime;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
class SearchNewsDtoMapperTest {
    @InjectMocks
    private SearchNewsDtoMapper mapper;

    @Test
    @DisplayName("convert: EcoNews -> SearchNewsDto (locale=en)")
    void convert_en_ok() {
        EcoNews entity = ModelUtils.getEcoNews();

        LocaleContextHolder.setLocale(new Locale("en"));

        SearchNewsDto expected = SearchNewsDto.builder()
            .id(1L)
            .title("title")
            .author(EcoNewsAuthorDto.builder()
                .id(1L)
                .name(TestConst.NAME)
                .build())
            .creationDate(zonedDateTime)
            .tags(List.of("News"))
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("convert: EcoNews -> SearchNewsDto (locale=ua)")
    void convert_ua_ok() {
        EcoNews entity = ModelUtils.getEcoNews();

        LocaleContextHolder.setLocale(new Locale("ua"));

        SearchNewsDto expected = SearchNewsDto.builder()
            .id(1L)
            .title("title")
            .author(EcoNewsAuthorDto.builder()
                .id(1L)
                .name(TestConst.NAME)
                .build())
            .creationDate(zonedDateTime)
            .tags(List.of("Новини"))
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    void convert_null() {
        EcoNews entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}