package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.econewscomment.EcoNewsCommentDto;
import greencity.entity.EcoNewsComment;
import greencity.entity.User;
import greencity.enums.CommentStatus; // якщо є
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class EcoNewsCommentDtoMapperTest {

    @InjectMocks
    private EcoNewsCommentDtoMapper mapper;

    @Test
    @DisplayName("Convert: Entity -> Dto with author fields")
    void convert_ok() {
        LocalDateTime now = LocalDateTime.now();
        EcoNewsComment entity = EcoNewsComment.builder()
                .id(1L)
                .text("text")
                .createdDate(now)
                .modifiedDate(now)
                .user(getUser())
                .ecoNews(getEcoNews())
                .deleted(false)
                .currentUserLiked(false)
                .usersLiked(Collections.emptySet())
                .build();
        EcoNewsCommentDto expected = EcoNewsCommentDto.builder()
                .id(1L)
                .modifiedDate(now)
                .author(getEcoNewsCommentAuthorDto())
                .text("text")
                .replies(0)
                .likes(0)
                .currentUserLiked(false)
                .status(CommentStatus.ORIGINAL)
                .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("Convert: null -> NPE")
    void convert_null() {
        EcoNewsComment entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}