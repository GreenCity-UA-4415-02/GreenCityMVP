package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddEventDtoResponse;
import greencity.dto.event.DateLocationDto;
import greencity.dto.tag.TagUaEnDto;
import greencity.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    private Principal principal;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        principal = () -> "user@example.com";
        objectMapper.findAndRegisterModules();
    }

    @Test
    void createEventTest() throws Exception {
        TagUaEnDto tag = TagUaEnDto.builder()
            .nameUa("Назва UA")
            .nameEn("Name EN")
            .build();

        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureFinish = LocalDateTime.now().plusDays(2);

        DateLocationDto dateLocation = DateLocationDto.builder()
            .startDate(futureStart)
            .finishDate(futureFinish)
            .address("Test Address")
            .latitude(null)
            .longitude(null)
            .onlineLink(null)
            .build();

        AddEventDtoRequest request = AddEventDtoRequest.builder()
            .title("Test Event")
            .description("This is a valid description for testing purposes.")
            .open(true)
            .tags(Collections.singletonList(tag))
            .datesLocations(Collections.singletonList(dateLocation))
            .build();

        AddEventDtoResponse response = AddEventDtoResponse.builder()
            .id(1L)
            .title(request.getTitle())
            .description(request.getDescription())
            .open(request.getOpen())
            .datesLocations(request.getDatesLocations())
            .tagNames(List.of(tag.getNameUa()))
            .images(Collections.emptyList())
            .build();

        when(eventService.create(any(AddEventDtoRequest.class), anyList(), anyString()))
            .thenReturn(response);

        String requestJson = objectMapper.writeValueAsString(request);

        MockMultipartFile jsonFile = new MockMultipartFile(
            "event",
            "event.json",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes());

        MockMultipartFile imageFile = new MockMultipartFile(
            "images",
            "image.png",
            MediaType.IMAGE_PNG_VALUE,
            "fake-image-content".getBytes());

        mockMvc.perform(multipart("/events/create")
            .file(jsonFile)
            .file(imageFile)
            .principal(principal)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated());

        verify(eventService).create(any(AddEventDtoRequest.class), anyList(), eq(principal.getName()));
    }

    @Test
    void createEvent_shouldFail_whenInvalidRequest() throws Exception {
        AddEventDtoRequest invalidRequest = AddEventDtoRequest.builder().build();
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        MockMultipartFile jsonFile = new MockMultipartFile(
            "event",
            "event.json",
            MediaType.APPLICATION_JSON_VALUE,
            requestJson.getBytes());

        mockMvc.perform(multipart("/events/create")
            .file(jsonFile)
            .principal(principal)
            .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest());

        verify(eventService, never()).create(any(), anyList(), anyString());
    }
}
