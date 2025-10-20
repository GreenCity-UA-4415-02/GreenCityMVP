package greencity.mapping;

import greencity.ModelUtils;
import greencity.TestConst;
import greencity.dto.econews.EcoNewsDto;
import greencity.entity.*;
import greencity.entity.localization.TagTranslation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class EcoNewsDtoMapperTest {

    @InjectMocks
    private EcoNewsDtoMapper mapper;

    @Test
    @DisplayName("convert: EcoNews -> EcoNewsDto")
    void convert_ok() {
        EcoNews entity = ModelUtils.getEcoNews();

        EcoNewsDto expected = EcoNewsDto.builder()
            .creationDate(entity.getCreationDate())
            .imagePath(entity.getImagePath())
            .id(entity.getId())
            .title(entity.getTitle())
            .content(entity.getText())
            .shortInfo(entity.getShortInfo())
            .author(ModelUtils.getEcoNewsAuthorDto())
            .tags(List.of("News"))
            .tagsUa(List.of("Новини"))
            .likes(entity.getUsersLikedNews().size())
            .dislikes(entity.getUsersDislikedNews().size())
            .countComments(1)
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("Convert: null -> NPE")
    void convert_null() {
        EcoNews entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}