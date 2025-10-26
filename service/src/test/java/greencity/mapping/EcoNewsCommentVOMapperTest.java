package greencity.mapping;

import greencity.dto.econews.EcoNewsVO;
import greencity.dto.econewscomment.EcoNewsCommentVO;
import greencity.dto.user.UserVO;
import greencity.entity.EcoNews;
import greencity.entity.EcoNewsComment;
import greencity.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class EcoNewsCommentVOMapperTest {

    @InjectMocks
    private EcoNewsCommentVOMapper mapper;

    @Test
    @DisplayName("Convert: Entity -> VO (відповідає реальній логіці мапера)")
    void convert_ok() {
        LocalDateTime now = LocalDateTime.now();

        User author = getUser();
        EcoNews news = getEcoNews();

        User liker1 = getUser().setId(101L);
        User liker2 = getUser().setId(202L);

        EcoNewsComment entity = EcoNewsComment.builder()
            .id(1L)
            .text("text")
            .createdDate(now)
            .modifiedDate(now)
            .user(author)
            .ecoNews(news)
            .deleted(false)
            .currentUserLiked(false)
            .usersLiked(Set.of(liker1, liker2))
            .build();

        EcoNewsCommentVO expected = EcoNewsCommentVO.builder()
            .id(1L)
            .text("text")
            .createdDate(now)
            .modifiedDate(now)
            .parentComment(null)
            .comments(null)
            .user(UserVO.builder()
                .id(author.getId())
                .role(author.getRole())
                .name(author.getName())
                .build())
            .ecoNews(EcoNewsVO.builder()
                .id(news.getId())
                .build())
            .deleted(false)
            .currentUserLiked(false)
            .usersLiked(Set.of(
                UserVO.builder().id(101L).build(),
                UserVO.builder().id(202L).build()))
            .build();

        assertEquals(expected, mapper.convert(entity));
    }

    @Test
    @DisplayName("Convert: null -> NPE")
    void convert_null() {
        EcoNewsComment entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }

    @Test
    @DisplayName("Convert: usersLiked = emptySet -> ок, порожній сет у VO")
    void convert_emptyUsersLiked_ok() {
        LocalDateTime now = LocalDateTime.now();

        EcoNewsComment entity = EcoNewsComment.builder()
            .id(2L)
            .text("another")
            .createdDate(now)
            .modifiedDate(now)
            .user(getUser())
            .ecoNews(getEcoNews())
            .deleted(false)
            .currentUserLiked(false)
            .usersLiked(Collections.emptySet())
            .build();

        EcoNewsCommentVO actual = mapper.convert(entity);

        assertNotNull(actual);
        assertNotNull(actual.getUsersLiked());
        assertTrue(actual.getUsersLiked().isEmpty());
    }
}