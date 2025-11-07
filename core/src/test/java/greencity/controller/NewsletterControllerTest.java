package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.subscription.SubscribeResultDto;
import greencity.dto.subscription.SubscriptionDto;
import greencity.dto.subscription.UnsubscriptionResultDto;
import greencity.service.NewsletterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NewsletterControllerTest {
    @Mock
    private NewsletterService newsletterService;
    @InjectMocks
    private NewsletterController newsletterController;
    private MockMvc mockMvc;

    private static final String NEWS_LETTER_LINK = "/api/newsletter";
    private static final String EMAIL = "test@gmail.com";

    private ObjectMapper objectMapper;
    private SubscriptionDto testSubscriptionDto;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(newsletterController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
        this.objectMapper = new ObjectMapper();
        this.testSubscriptionDto = SubscriptionDto.builder()
            .email(EMAIL)
            .source("QR")
            .build();
    }

    @Test
    @DisplayName("Test subscribe should return 200 OK")
    void subscribeStatus200() throws Exception {
        SubscribeResultDto expectedSubscribeResultDto = SubscribeResultDto.builder()
            .ok(true)
            .alreadySubscribed(false)
            .build();
        when(newsletterService.subscribe(testSubscriptionDto)).thenReturn(expectedSubscribeResultDto);

        mockMvc.perform(post(NEWS_LETTER_LINK + "/subscribe")
            .content(objectMapper.writeValueAsString(testSubscriptionDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.alreadySubscribed").value(false));
        verify(newsletterService).subscribe(testSubscriptionDto);
    }

    @Test
    @DisplayName("Test subscribe should return 200 OK when email is already subscribed")
    void subscribeStatusAlreadySubscribed() throws Exception {
        SubscribeResultDto expectedSubscribeResultDto = SubscribeResultDto.builder()
            .ok(true)
            .alreadySubscribed(true)
            .build();

        when(newsletterService.subscribe(testSubscriptionDto)).thenReturn(expectedSubscribeResultDto);

        mockMvc.perform(post(NEWS_LETTER_LINK + "/subscribe")
            .content(objectMapper.writeValueAsString(testSubscriptionDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.alreadySubscribed").value(true));

        verify(newsletterService).subscribe(testSubscriptionDto);
    }

    @Test
    @DisplayName("Test subscribe should return 400 BAD_REQUEST")
    void subscribeStatus400() throws Exception {
        SubscriptionDto invalidSubscriptionDto = SubscriptionDto.builder()
            .email("234@")
            .source("QR")
            .build();
        mockMvc.perform(post(NEWS_LETTER_LINK + "/subscribe")
            .content(objectMapper.writeValueAsString(invalidSubscriptionDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
        verify(newsletterService, never()).subscribe(any(SubscriptionDto.class));
    }

    @Test
    @DisplayName("Test unsubscribe should return 200 OK")
    void unsubscribeStatus200() throws Exception {
        UnsubscriptionResultDto expectedUnsubscriptionResultDto = UnsubscriptionResultDto.builder()
            .ok(true)
            .alreadySubscribed(true)
            .status("unsubscribed")
            .build();
        when(newsletterService.unsubscribe(EMAIL)).thenReturn(expectedUnsubscriptionResultDto);

        mockMvc.perform(post(NEWS_LETTER_LINK + "/unsubscribe")
            .content(objectMapper.writeValueAsString(testSubscriptionDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ok").value(true))
            .andExpect(jsonPath("$.alreadySubscribed").value(true))
            .andExpect(jsonPath("$.status").value("unsubscribed"));
        verify(newsletterService).unsubscribe(EMAIL);
    }

    @Test
    @DisplayName("Test unsubscribe should return 400")
    void unsubscribeStatus400() throws Exception {
        SubscriptionDto invalidSubscriptionDto = SubscriptionDto.builder()
            .email("234@")
            .source("QR")
            .build();
        mockMvc.perform(post(NEWS_LETTER_LINK + "/unsubscribe")
            .content(objectMapper.writeValueAsString(invalidSubscriptionDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
        verify(newsletterService, never()).subscribe(any(SubscriptionDto.class));
    }
}