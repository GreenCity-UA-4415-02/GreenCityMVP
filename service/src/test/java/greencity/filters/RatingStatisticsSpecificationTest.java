package greencity.filters;

import greencity.annotations.RatingCalculationEnum;
import greencity.entity.RatingStatistics;
import greencity.entity.RatingStatistics_;
import greencity.entity.User;
import greencity.entity.User_;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RatingStatisticsSpecificationTest {
    @Mock
    private CriteriaBuilder criteriaBuilder;
    @Mock
    private CriteriaQuery<?> criteriaQuery;
    @Mock
    private Root<RatingStatistics> root;

    @Mock
    private Predicate predicateTrue;
    @Mock
    private Predicate predicateAnd1;
    @Mock
    private Predicate predicateAnd2;
    @Mock
    private Predicate predicateAnd3;
    @Mock
    private Predicate predicateEnumOr1;
    @Mock
    private Predicate predicateEnumOr2;
    @Mock
    private Predicate predicateEnumEq1;
    @Mock
    private Predicate predicateEnumEq2;
    @Mock
    private Predicate predicateUserId;
    @Mock
    private Predicate predicateUserMail;
    @Mock
    private Predicate predicateNumeric;
    @Mock
    private Predicate predicateDateRange;

    @Mock
    private Path<Object> enumPath;
    @Mock
    private Path<String> emailPath;
    @Mock
    private Path<Long> userIdPath;
    @Mock
    private Path<Object> numericPath;
    @Mock
    private Path<Object> datePath;

    @Mock
    private Join<RatingStatistics, User> userJoin;

    private AutoCloseable closeable;

    @BeforeEach
    void init() {
        closeable = MockitoAnnotations.openMocks(this);
        when(criteriaBuilder.conjunction()).thenReturn(predicateTrue);
        when(criteriaBuilder.disjunction()).thenReturn(predicateEnumOr1);

        // Mock join for user-related tests
        when(root.join(RatingStatistics_.user)).thenReturn(userJoin);
        when(userJoin.get(User_.email)).thenReturn(emailPath);
        when(userJoin.get(User_.id)).thenReturn(userIdPath);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // Constructor tests
    @Test
    void constructor_withEmptyList_createsInstance() {
        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(Arrays.asList());
        assertNotNull(spec);
    }

    @Test
    void constructor_withCriteriaList_createsInstance() {
        SearchCriteria criteria = SearchCriteria.builder().type("id").key("id").value(1L).build();
        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(Arrays.asList(criteria));
        assertNotNull(spec);
    }

    // Basic functionality tests
    @Test
    void toPredicate_emptyCriteriaList_returnsConjunction() {
        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(Arrays.asList());
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertSame(predicateTrue, result);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void toPredicate_unknownType_ignored() {
        SearchCriteria unknown = SearchCriteria.builder().type("unknown").key("unknown").value("value").build();
        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(Arrays.asList(unknown));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        assertSame(predicateTrue, result);
        verify(criteriaBuilder).conjunction();
    }

    // ID filter tests
    @Test
    void toPredicate_id_numericFilter() {
        SearchCriteria c = SearchCriteria.builder().type("id").key("id").value(123L).build();
        when(root.get("id")).thenReturn(numericPath);
        when(criteriaBuilder.equal(numericPath, 123L)).thenReturn(predicateNumeric);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).equal(numericPath, 123L);
    }

    @Test
    void toPredicate_id_invalidValue_returnsDisjunction() {
        SearchCriteria c = SearchCriteria.builder().type("id").key("id").value("invalid").build();
        when(root.get("id")).thenReturn(numericPath);
        when(criteriaBuilder.equal(numericPath, "invalid")).thenThrow(new NumberFormatException());
        when(criteriaBuilder.disjunction()).thenReturn(predicateNumeric);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).disjunction();
    }

    // Points changed filter tests
    @Test
    void toPredicate_pointsChanged_numericFilter() {
        SearchCriteria c = SearchCriteria.builder().type("pointsChanged").key("pointsChanged").value(5).build();
        when(root.get("pointsChanged")).thenReturn(numericPath);
        when(criteriaBuilder.equal(numericPath, 5)).thenReturn(predicateNumeric);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).equal(numericPath, 5);
    }

    // Current rating filter tests
    @Test
    void toPredicate_currentRating_numericFilter() {
        SearchCriteria c = SearchCriteria.builder().type("currentRating").key("currentRating").value(100.5).build();
        when(root.get("currentRating")).thenReturn(numericPath);
        when(criteriaBuilder.equal(numericPath, 100.5)).thenReturn(predicateNumeric);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).equal(numericPath, 100.5);
    }

    // Enum filter tests
    @Test
    void toPredicate_enum_partialMatch_buildsOrOverMatches() {
        SearchCriteria c = SearchCriteria.builder().type("enum").key("eventName").value("comment").build();
        when(root.get("eventName")).thenReturn(enumPath);
        when(criteriaBuilder.equal(enumPath, RatingCalculationEnum.ADD_COMMENT)).thenReturn(predicateEnumEq1);
        when(criteriaBuilder.equal(enumPath, RatingCalculationEnum.DELETE_COMMENT)).thenReturn(predicateEnumEq2);
        when(criteriaBuilder.or(any(), any())).thenReturn(predicateEnumOr2);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).equal(enumPath, RatingCalculationEnum.ADD_COMMENT);
        verify(criteriaBuilder).equal(enumPath, RatingCalculationEnum.DELETE_COMMENT);
    }

    @Test
    void toPredicate_enum_noMatches_returnsDisjunction() {
        SearchCriteria c = SearchCriteria.builder().type("enum").key("eventName").value("nonexistent").build();
        when(root.get("eventName")).thenReturn(enumPath);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).disjunction();
    }

    // User ID filter tests
    @Test
    void toPredicate_userId_joinAndEqual() {
        SearchCriteria c = SearchCriteria.builder().type("userId").key("ignored").value(42L).build();
        when(criteriaBuilder.equal(userIdPath, 42L)).thenReturn(predicateUserId);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).equal(userIdPath, 42L);
        verify(root).join(RatingStatistics_.user);
        verify(userJoin).get(User_.id);
    }

    @Test
    void toPredicate_userId_invalidValue_returnsDisjunction() {
        SearchCriteria c = SearchCriteria.builder().type("userId").key("ignored").value("invalid").build();
        when(criteriaBuilder.equal(userIdPath, "invalid")).thenThrow(new NumberFormatException());
        when(criteriaBuilder.disjunction()).thenReturn(predicateUserId);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).disjunction();
    }

    // User mail filter tests

    @Test
    void toPredicate_dateRange_validDates() {
        String[] dates = {"2023-01-01", "2023-12-31"};
        SearchCriteria c = SearchCriteria.builder().type("dateRange").key("createdAt").value(dates).build();
        when(root.get("createdAt")).thenReturn(datePath);
        when(criteriaBuilder.between(any(Expression.class), any(Comparable.class), any(Comparable.class))).thenReturn(predicateDateRange);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd1, result);
        verify(criteriaBuilder).between(any(Expression.class), any(Comparable.class), any(Comparable.class));
    }

    @Test
    void toPredicate_dateRange_invalidDates_returnsConjunction() {
        String[] dates = {"invalid-date", "2023-12-31"};
        SearchCriteria c = SearchCriteria.builder().type("dateRange").key("createdAt").value(dates).build();
        when(root.get("createdAt")).thenReturn(datePath);
        when(criteriaBuilder.between(any(Expression.class), any(Comparable.class), any(Comparable.class))).thenThrow(new RuntimeException());
        when(criteriaBuilder.conjunction()).thenReturn(predicateTrue);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateTrue);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(List.of(c));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateTrue, result);
        verify(criteriaBuilder).conjunction();
    }

    // Compound filter tests
    @Test
    void toPredicate_compound_multipleCriteria_andLogic() {
        SearchCriteria userId = SearchCriteria.builder().type("userId").key("ignored").value(42L).build();
        SearchCriteria mail = SearchCriteria.builder().type("userMail").key("ignored").value("user@ex").build();
        SearchCriteria id = SearchCriteria.builder().type("id").key("id").value(123L).build();

        when(criteriaBuilder.equal(userIdPath, 42L)).thenReturn(predicateUserId);
        when(criteriaBuilder.like(emailPath, "%user@ex%")).thenReturn(predicateUserMail);
        when(root.get("id")).thenReturn(numericPath);
        when(criteriaBuilder.equal(numericPath, 123L)).thenReturn(predicateNumeric);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicateAnd1, predicateAnd2, predicateAnd3);

        RatingStatisticsSpecification spec = new RatingStatisticsSpecification(Arrays.asList(userId, mail, id));
        Predicate result = spec.toPredicate(root, criteriaQuery, criteriaBuilder);
        
        assertSame(predicateAnd3, result);
        verify(criteriaBuilder, atLeast(2)).and(any(), any());
    }
}