package greencity.dto.subscription;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscribeResultDtoTest {

    @Test
    @DisplayName("Test case when subscription is successful (OK, not previously subscribed)")
    void isOkAndNewSubscription() {
        SubscribeResultDto exceptedDto = SubscribeResultDto.builder()
            .ok(true)
            .alreadySubscribed(false)
            .build();

        assertTrue(exceptedDto.isOk(), "The 'ok' field must be true for a successful operation.");
        assertFalse(exceptedDto.isAlreadySubscribed(),
            "The 'alreadySubscribed' field must be false for a new subscription.");
    }

    @Test
    @DisplayName("Test case when email was already subscribed (OK, already subscribed)")
    void isOkButAlreadySubscribed() {
        SubscribeResultDto exceptedDto = SubscribeResultDto.builder()
            .ok(true)
            .alreadySubscribed(true)
            .build();

        assertTrue(exceptedDto.isOk(), "The 'ok' field must be true.");
        assertTrue(exceptedDto.isAlreadySubscribed(), "The 'alreadySubscribed' field must be true.");
    }

    @Test
    @DisplayName("Test case for constructor and getters/setters")
    void testStandardConstructorAndAccessors() {
        SubscribeResultDto defaultDto = new SubscribeResultDto();
        defaultDto.setOk(true);
        defaultDto.setAlreadySubscribed(false);

        assertTrue(defaultDto.isOk());
        assertFalse(defaultDto.isAlreadySubscribed());

        SubscribeResultDto fullDto = new SubscribeResultDto(false, true);
        assertFalse(fullDto.isOk());
        assertTrue(fullDto.isAlreadySubscribed());
    }
}