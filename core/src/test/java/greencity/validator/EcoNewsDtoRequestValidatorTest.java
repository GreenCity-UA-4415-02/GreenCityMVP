package greencity.validator;

import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.exception.exceptions.WrongCountOfTagsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static greencity.ModelUtils.getAddEcoNewsDtoRequest;
import static org.junit.jupiter.api.Assertions.*;

class EcoNewsDtoRequestValidatorTest {

    private EcoNewsDtoRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EcoNewsDtoRequestValidator();
        validator.initialize(null);
    }



    @Test
    void isValid_ShouldReturnTrue_WhenTagsValidAndSourceNull() {
        AddEcoNewsDtoRequest dto = getAddEcoNewsDtoRequest();
        dto.setTags(Arrays.asList("tag1", "tag2"));
        dto.setSource(null);

        assertTrue(validator.isValid(dto, null));
    }

    @Test
    void isValid_ShouldReturnTrue_WhenTagsValidAndSourceEmpty() {
        AddEcoNewsDtoRequest dto = getAddEcoNewsDtoRequest();
        dto.setTags(Collections.singletonList("tag1"));
        dto.setSource("");

        assertTrue(validator.isValid(dto, null));
    }

    @Test
    void isValid_ShouldReturnTrue_WhenTagsCountIsOne() {
        AddEcoNewsDtoRequest dto = getAddEcoNewsDtoRequest();
        dto.setTags(Collections.singletonList("tag1"));
        dto.setSource(null);

        assertTrue(validator.isValid(dto, null));
    }

    @Test
    void isValid_ShouldReturnTrue_WhenTagsCountIsMax() {
        AddEcoNewsDtoRequest dto = getAddEcoNewsDtoRequest();
        dto.setTags(Arrays.asList("tag1", "tag2", "tag3"));
        dto.setSource(null);

        assertTrue(validator.isValid(dto, null));
    }


    @Test
    void isValid_ShouldReturnTrue_WhenSourceIsRealUrl() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setSource("https://eco-lavca.ua/");
        assertTrue(validator.isValid(request, null));
    }


    @Test
    void isValid_ShouldThrow_WhenTagsEmpty() {
        AddEcoNewsDtoRequest dto = getAddEcoNewsDtoRequest();
        dto.setTags(Collections.emptyList());
        dto.setSource(null);

        WrongCountOfTagsException exception = assertThrows(WrongCountOfTagsException.class,
                () -> validator.isValid(dto, null));
        assertNotNull(exception.getMessage());
    }

    @Test
    void isValid_ShouldThrow_WhenTagsExceedMaxAmount() {
        AddEcoNewsDtoRequest dto = getAddEcoNewsDtoRequest();
        dto.setTags(Arrays.asList("tag1", "tag2", "tag3", "tag4"));
        dto.setSource(null);

        WrongCountOfTagsException exception = assertThrows(WrongCountOfTagsException.class,
                () -> validator.isValid(dto, null));
        assertNotNull(exception.getMessage());
    }
}

