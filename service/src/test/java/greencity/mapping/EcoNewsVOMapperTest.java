package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.econews.EcoNewsVO;
import greencity.dto.econewscomment.EcoNewsCommentVO;
import greencity.dto.user.UserVO;
import greencity.entity.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class EcoNewsVOMapperTest {

    @InjectMocks
    private EcoNewsVOMapper mapper;

    @Test
    @DisplayName("Convert: maps full graph using ModelUtils")
    void convert_full_ok() {
        // 1) Базова новина з ModelUtils
        EcoNews entity = ModelUtils.getEcoNews();

        // 2) Коментар з користувачем (у getEcoNews() він null)
        EcoNewsComment comment = ModelUtils.getEcoNewsComment();
        entity.setEcoNewsComments(List.of(comment));

        // 3) Лайки/дизлайки (будь-які юзери підійдуть)
        entity.setUsersLikedNews(Set.of(ModelUtils.getUser()));
        entity.setUsersDislikedNews(Set.of(ModelUtils.TEST_USER_ROLE_USER));

        // 4) Теги теж можна взяти готові (у getEcoNews() вже є, але так очевидніше)
        entity.setTags(ModelUtils.getTags());

        // 5) Мапимо
        EcoNewsVO vo = mapper.convert(entity);

        // 6) Перевіряємо ключові поля (без тотального equals())
        assertNotNull(vo);
        assertEquals(entity.getId(), vo.getId());
        assertEquals(entity.getCreationDate(), vo.getCreationDate());
        assertEquals(entity.getImagePath(), vo.getImagePath());
        assertEquals(entity.getSource(), vo.getSource());
        assertEquals(entity.getTitle(), vo.getTitle());
        assertEquals(entity.getText(), vo.getText());

        // author
        assertNotNull(vo.getAuthor());
        assertEquals(entity.getAuthor().getId(), vo.getAuthor().getId());
        assertEquals(entity.getAuthor().getRole(), vo.getAuthor().getRole());
        assertEquals(entity.getAuthor().getUserStatus(), vo.getAuthor().getUserStatus());

        // tags (перевіримо кількість і мови перекладів)
        assertEquals(entity.getTags().size(), vo.getTags().size());
        var voTag = vo.getTags().get(0);
        assertNotNull(voTag.getTagTranslations());
        assertTrue(voTag.getTagTranslations().stream()
                .anyMatch(t -> "ua".equals(t.getLanguageVO().getCode())));
        assertTrue(voTag.getTagTranslations().stream()
                .anyMatch(t -> "en".equals(t.getLanguageVO().getCode())));

        // likes / dislikes
        assertEquals(
                entity.getUsersLikedNews().stream().map(User::getId).collect(Collectors.toSet()),
                vo.getUsersLikedNews().stream().map(UserVO::getId).collect(Collectors.toSet())
        );
        assertEquals(
                entity.getUsersDislikedNews().stream().map(User::getId).collect(Collectors.toSet()),
                vo.getUsersDislikedNews().stream().map(UserVO::getId).collect(Collectors.toSet())
        );

        // comments
        assertEquals(entity.getEcoNewsComments().size(), vo.getEcoNewsComments().size());
        EcoNewsComment srcComment = entity.getEcoNewsComments().get(0);
        EcoNewsCommentVO dstComment = vo.getEcoNewsComments().get(0);
        assertEquals(srcComment.getId(), dstComment.getId());
        assertEquals(srcComment.getText(), dstComment.getText());
        assertNotNull(dstComment.getUser());
        assertEquals(srcComment.getUser().getId(), dstComment.getUser().getId());
    }

    @Test
    @DisplayName("Convert: null -> NPE")
    void convert_null() {
        EcoNews entity = null;

        assertThrows(NullPointerException.class, () -> mapper.convert(entity));
    }
}