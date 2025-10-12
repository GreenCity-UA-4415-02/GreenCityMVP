package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddEventDtoResponse;
import greencity.dto.event.DateLocationDto;
import greencity.dto.tag.TagUaEnDto;
import greencity.service.EventService;
import lombok.SneakyThrows;
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

    @Mock
    private ObjectMapper objectMapper;

    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        principal = () -> "user@example.com";
    }

    @Test
    @SneakyThrows
    void createEventTest() {
        AddEventDtoRequest request = AddEventDtoRequest.builder()
                .title("Test Event")
                .description("This is a valid description for testing purposes.")
                .open(true)
                .tags(Collections.singletonList(new TagUaEnDto("тест", "test")))
                .datesLocations(Collections.singletonList(
                        new DateLocationDto(
                                LocalDateTime.now().plusDays(1),
                                LocalDateTime.now().plusDays(2),
                                "Some Address"
                        )
                ))
                .build();

        AddEventDtoResponse response = AddEventDtoResponse.builder()
                .id(1L)
                .title(request.getTitle())
                .description(request.getDescription())
                .open(request.getOpen())
                .images(Collections.emptyList())
                .datesLocations(request.getDatesLocations())
                .tagNames(List.of("тест"))
                .build();

        when(eventService.create(eq(request), isNull(), eq(principal.getName()))).thenReturn(response);

        // --- JSON part ---
        ObjectMapper mapper = new ObjectMapper();
        MockMultipartFile jsonFile = new MockMultipartFile(
                "event",                   // має збігатися з @RequestPart("event")
                "",
                "application/json",
                mapper.writeValueAsBytes(request)
        );

        // --- Image part ---
        MockMultipartFile imageFile = new MockMultipartFile(
                "images",
                "image.png",
                "image/png",
                "dummy image content".getBytes()
        );

        mockMvc.perform(multipart("/events/create")
                        .file(jsonFile)
                        .file(imageFile)
                        .principal(principal)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        verify(eventService).create(eq(request), anyList(), eq(principal.getName()));
    }
}
