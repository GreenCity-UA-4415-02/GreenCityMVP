package greencity.filters;

import greencity.entity.ShoppingListItem;
import greencity.entity.localization.ShoppingListItemTranslation;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShoppingListItemSpecificationTest {
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<?> criteriaQuery;
    @Mock
    private Root<ShoppingListItem> root;
    @Mock
    private Root<ShoppingListItemTranslation> translationRoot;

    @Mock
    private Predicate predicateTrue;
    @Mock
    private Predicate predicateId;
    @Mock
    private Predicate predicateContentLike;
    @Mock
    private Predicate predicateJoinEq;
    @Mock
    private Predicate predicateContent;
    @Mock
    private Predicate predicateAnd1;
    @Mock
    private Predicate predicateAnd2;

    @Mock
    private Path<Object> idPath;
    @Mock
    private Path<String> contentPath;
    @Mock
    private Path<ShoppingListItem> itemPath;
    @Mock
    private Path<Long> itemIdPath;
    @Mock
    private Path<Long> rootIdPathFromMeta;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        when(criteriaBuilder.conjunction()).thenReturn(predicateTrue);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void toPredicate_idOnly_buildsNumericPredicate() {
        SearchCriteria idCriteria = SearchCriteria.builder()
            .type("id")
            .key("id")
            .value(123L)
            .build();

        when(root.get("id")).thenReturn(idPath);
        when(criteriaBuilder.equal(idPath, 123L)).thenReturn(predicateId);
        when(criteriaBuilder.and(predicateTrue, predicateId)).thenReturn(predicateAnd1);

        ShoppingListItemSpecification spec = new ShoppingListItemSpecification(List.of(idCriteria));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder).equal(idPath, 123L);
        verify(criteriaBuilder).and(predicateTrue, predicateId);
    }

    @Test
    void toPredicate_contentOnly_buildsJoinAndLikePredicate() {
        SearchCriteria contentCriteria = SearchCriteria.builder()
            .type("content")
            .key("ignored")
            .value("milk")
            .build();

        when(criteriaQuery.from(ShoppingListItemTranslation.class)).thenReturn(translationRoot);
        org.mockito.Mockito.doReturn(contentPath).doReturn(itemPath)
            .when(translationRoot).get(org.mockito.ArgumentMatchers.<SingularAttribute>any());
        org.mockito.Mockito.doReturn(itemIdPath).when(itemPath)
            .get(org.mockito.ArgumentMatchers.<SingularAttribute>any());
        org.mockito.Mockito.doReturn(rootIdPathFromMeta).when(root)
            .get(org.mockito.ArgumentMatchers.<SingularAttribute>any());

        when(criteriaBuilder.like(contentPath, "%milk%"))
            .thenReturn(predicateContentLike);
        when(criteriaBuilder.equal(itemIdPath, rootIdPathFromMeta))
            .thenReturn(predicateJoinEq);
        when(criteriaBuilder.and(predicateContentLike, predicateJoinEq))
            .thenReturn(predicateContent);
        when(criteriaBuilder.and(predicateTrue, predicateContent))
            .thenReturn(predicateAnd1);

        ShoppingListItemSpecification spec = new ShoppingListItemSpecification(List.of(contentCriteria));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(predicateAnd1, result);
        verify(criteriaQuery, times(1)).from(ShoppingListItemTranslation.class);
        verify(criteriaBuilder, times(1)).like(contentPath, "%milk%");
        verify(criteriaBuilder, times(1)).equal(itemIdPath, rootIdPathFromMeta);
        verify(criteriaBuilder, times(1)).and(predicateContentLike, predicateJoinEq);
        verify(criteriaBuilder, times(1)).and(predicateTrue, predicateContent);
    }

    @Test
    void toPredicate_contentBlank_skipsFiltering() {
        SearchCriteria contentCriteria = SearchCriteria.builder()
            .type("content")
            .key("ignored")
            .value("   ")
            .build();

        when(criteriaQuery.from(ShoppingListItemTranslation.class)).thenReturn(translationRoot);
        when(criteriaBuilder.and(predicateTrue, predicateTrue)).thenReturn(predicateTrue);

        ShoppingListItemSpecification spec = new ShoppingListItemSpecification(List.of(contentCriteria));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(predicateTrue, result);
        verify(criteriaQuery).from(ShoppingListItemTranslation.class);
        verify(criteriaBuilder, never()).like(any(), any(String.class));
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    void toPredicate_compound_idAndContent() {
        SearchCriteria idCriteria = SearchCriteria.builder()
            .type("id")
            .key("id")
            .value(5L)
            .build();
        SearchCriteria contentCriteria = SearchCriteria.builder()
            .type("content")
            .key("ignored")
            .value("bread")
            .build();

        when(root.get("id")).thenReturn(idPath);
        when(criteriaBuilder.equal(idPath, 5L)).thenReturn(predicateId);

        when(criteriaQuery.from(ShoppingListItemTranslation.class)).thenReturn(translationRoot);
        org.mockito.Mockito.doReturn(contentPath).doReturn(itemPath)
            .when(translationRoot).get(org.mockito.ArgumentMatchers.<SingularAttribute>any());
        org.mockito.Mockito.doReturn(itemIdPath).when(itemPath)
            .get(org.mockito.ArgumentMatchers.<SingularAttribute>any());
        org.mockito.Mockito.doReturn(rootIdPathFromMeta).when(root)
            .get(org.mockito.ArgumentMatchers.<SingularAttribute>any());

        when(criteriaBuilder.like(contentPath, "%bread%"))
            .thenReturn(predicateContentLike);
        when(criteriaBuilder.equal(itemIdPath, rootIdPathFromMeta))
            .thenReturn(predicateJoinEq);
        when(criteriaBuilder.and(predicateContentLike, predicateJoinEq))
            .thenReturn(predicateContent);

        when(criteriaBuilder.and(predicateTrue, predicateId)).thenReturn(predicateAnd1);
        when(criteriaBuilder.and(predicateAnd1, predicateContent)).thenReturn(predicateAnd2);

        ShoppingListItemSpecification spec =
            new ShoppingListItemSpecification(Arrays.asList(idCriteria, contentCriteria));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(predicateAnd2, result);
        verify(criteriaBuilder).and(predicateTrue, predicateId);
        verify(criteriaBuilder).and(predicateContentLike, predicateJoinEq);
        verify(criteriaBuilder).and(predicateAnd1, predicateContent);
    }

    @Test
    void toPredicate_unknownType_isIgnored() {
        SearchCriteria unknown = SearchCriteria.builder()
            .type("unknown")
            .key("whatever")
            .value("x")
            .build();

        ShoppingListItemSpecification spec = new ShoppingListItemSpecification(List.of(unknown));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);

        assertSame(predicateTrue, result);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).and(any(), any());
    }
}
