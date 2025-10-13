package greencity.dto.subscription;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Parameterized unit tests for {@link SubscriptionDto} validation constraints.
 */
class SubscriptionDtoTest{

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private int validate(SubscriptionDto dto) {
        return validator.validate(dto).size();
    }

    @Test
    @DisplayName("Test valid SubscriptionDto should have no violations")
    void testValidSubscriptionDto() {
        SubscriptionDto dto = SubscriptionDto.builder()
                                             .email("valid.email@example.com")
                                             .source("LANDING")
                                             .build();

        assertEquals(0, validate(dto), "Valid DTO should have 0 violations");
    }


    @ParameterizedTest(name = "{index}: Source '{0}' -> Expected Violations: {1}")
    @CsvSource(value = {
            "'',2",
            "APP,1",
            "mobile,1"
    }, ignoreLeadingAndTrailingWhitespace = true)
    @DisplayName("Parameterized test for invalid Source constraints")
    void testInvalidSources(String source, int expectedViolations) {
        SubscriptionDto dto = SubscriptionDto.builder()
                                             .email("test@example.com")
                                             .source(source)
                                             .build();

        if ("".equals(source)) {
            assertEquals(expectedViolations, validate(dto), "Violation count mismatch for Source");
        } else {
            assertEquals(expectedViolations, validate(dto), "Violation count mismatch for Source");
        }
    }

    @ParameterizedTest(name = "{index}: Valid Source '{0}' should pass")
    @CsvSource(value = {
            "LANDING",
            "QR",
            "landing",
            "qr"
    })
    @DisplayName("Parameterized test for valid Source constraints (case-insensitive)")
    void testValidSources(String source) {
        SubscriptionDto dto = SubscriptionDto.builder()
                                             .email("test@example.com")
                                             .source(source)
                                             .build();

        assertEquals(0, validate(dto), "Valid source should have 0 violations");
    }
}