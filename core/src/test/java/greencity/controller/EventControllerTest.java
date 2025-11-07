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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import greencity.annotations.CurrentUser;
import greencity.dto.event.*;
import greencity.dto.user.UserVO;
import greencity.enums.EventStatus;
import greencity.enums.EventType;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.util.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

        var err = new ForcedMessageErrorAttributes();
        var advice = new CustomExceptionHandler(err, objectMapper);

        var jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new TestUserArgumentResolver(mockUser))
            .setControllerAdvice(advice)
            .setMessageConverters(jacksonConverter)
            .build();
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

    @InjectMocks
    EventController controller;

    // ---- Helpers ----
    static class ForcedMessageErrorAttributes extends DefaultErrorAttributes {
        @Override
        public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
            var newOptions = options.including(
                ErrorAttributeOptions.Include.MESSAGE,
                ErrorAttributeOptions.Include.EXCEPTION);
            return super.getErrorAttributes(webRequest, newOptions);
        }
    }

    static class TestUserArgumentResolver implements HandlerMethodArgumentResolver {
        private final UserVO user;

        TestUserArgumentResolver(UserVO user) {
            this.user = user;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterAnnotation(CurrentUser.class) != null
                && parameter.getParameterType().equals(UserVO.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return user;
        }
    }

    private byte[] jpg(int sizeBytes) {
        byte[] header = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        byte[] out = new byte[sizeBytes];
        System.arraycopy(header, 0, out, 0, Math.min(header.length, out.length));
        return out;
    }

    private byte[] jpg() {
        return jpg(1024);
    }

    private byte[] png() {
        byte[] header = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        byte[] rest = new byte[256];
        byte[] out = new byte[header.length + rest.length];
        System.arraycopy(header, 0, out, 0, header.length);
        return out;
    }

    // ---- Common test data ----
    UserVO mockUser = UserVO.builder().id(5L).email("test@example.com").name("Tester").build();

    DateLocationDto loc = DateLocationDto.builder()
        .startDate(LocalDateTime.now().plusDays(1))
        .finishDate(LocalDateTime.now().plusDays(1).plusHours(2))
        .latitude(BigDecimal.valueOf(50.45))
        .longitude(BigDecimal.valueOf(30.52))
        .build();

    AddEventDtoRequest addReq = AddEventDtoRequest.builder()
        .title("Eco Cleanup").description("Let’s clean the park together!")
        .open(true).datesLocations(List.of(loc)).build();

    UpdateEventDtoRequest updReq = UpdateEventDtoRequest.builder()
        .title("Updated Title").description("Updated long description")
        .datesLocations(List.of(loc)).build();

    // ============================
    // POST /events/create
    // ============================

    @Test
    void createEvent_badRequestFromService_shouldReturn400() throws Exception {
        String json = objectMapper.writeValueAsString(addReq);
        MockMultipartFile dto = new MockMultipartFile("event", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());

        when(eventService.create(any(), anyList(), anyString()))
            .thenThrow(new BadRequestException("Too many images"));

        mockMvc.perform(multipart("/events/create").file(dto)
            .contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON)
            .principal((Principal) () -> "test@example.com"))
            .andExpect(status().isBadRequest());
    }

    // ============================
    // GET /events/visible
    // ============================
    @Test
    void getVisibleEvents_shouldReturn200WithList() throws Exception {
        EventDto e1 = EventDto.builder().id(1L).title("Public Cleanup").isOpen(true).build();
        EventDto e2 = EventDto.builder().id(2L).title("Friends Only").isOpen(false).build();
        when(eventService.getVisibleEvents("test@example.com")).thenReturn(List.of(e1, e2));

        mockMvc.perform(get("/events/visible").accept(MediaType.APPLICATION_JSON)
            .principal((Principal) () -> "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title").value("Public Cleanup"))
            .andExpect(jsonPath("$[1].open").value(false));

        verify(eventService).getVisibleEvents("test@example.com");
    }

    @Test
    void getVisibleEvents_empty_shouldReturn200EmptyList() throws Exception {
        when(eventService.getVisibleEvents("test@example.com")).thenReturn(List.of());

        mockMvc.perform(get("/events/visible").accept(MediaType.APPLICATION_JSON)
                        .principal((Principal) () -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ============================
    // GET /events/myEvents
    // ============================
    @Test
    void getMyEvents_ok_withTypeAndCoords() throws Exception {
        EventPreviewDto preview = EventPreviewDto.builder()
            .id(10L)
            .title("Place Event")
            .status(EventStatus.UPCOMING)
            .nearestStart(LocalDateTime.now().plusDays(1))
            .types(EventTypesDto.builder().place(true).online(false).build())
            .distance(1.23).build();

        Page<EventPreviewDto> page = new PageImpl<>(List.of(preview), PageRequest.of(0, 10), 1);
        when(eventService.getMyEvents(eq(5L), eq(EventType.PLACE), isNull(), eq(50.45), eq(30.52), any(Pageable.class)))
            .thenReturn(page);

        mockMvc.perform(get("/events/myEvents")
            .param("eventType", "PLACE")
            .param("userLatitude", "50.45")
            .param("userLongitude", "30.52")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(10))
            .andExpect(jsonPath("$.content[0].status").value("UPCOMING"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getMyEvents_invalidStatus_shouldReturn400() throws Exception {
        mockMvc.perform(get("/events/myEvents")
            .param("status", "INVALID")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    // ============================
    // GET /events/myEvents/createdEvents
    // ============================
    @Test
    void getMyCreatedEvents_ok_withStatus() throws Exception {
        EventPreviewDto dto = EventPreviewDto.builder()
            .id(2L).title("My Created").status(EventStatus.LIVE).canEdit(true).build();
        Page<EventPreviewDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(eventService.getMyCreatedEvents(eq(5L), eq(EventStatus.LIVE), any(Pageable.class)))
            .thenReturn(page);

        mockMvc.perform(get("/events/myEvents/createdEvents")
            .param("status", "LIVE")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value("LIVE"))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getMyCreatedEvents_invalidStatus_shouldReturn400() throws Exception {
        mockMvc.perform(get("/events/myEvents/createdEvents")
            .param("status", "BAD")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    // ============================
    // GET /events/myEvents/relatedEvents
    // ============================
    @Test
    void getRelatedEvents_ok() throws Exception {
        EventPreviewDto a = EventPreviewDto.builder().id(1L).title("Created").canEdit(true).build();
        EventPreviewDto b = EventPreviewDto.builder().id(2L).title("Joined").canEdit(false).build();
        Page<EventPreviewDto> page = new PageImpl<>(List.of(a, b), PageRequest.of(0, 10), 2);

        when(eventService.getRelatedEvents(eq(5L), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/events/myEvents/relatedEvents").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].title").value("Created"))
            .andExpect(jsonPath("$.content[1].canEdit").value(false));
    }

    @Test
    void getRelatedEvents_withStatusFilter() throws Exception {
        EventPreviewDto dto = EventPreviewDto.builder().id(3L).title("Upcoming X").status(EventStatus.UPCOMING).build();
        Page<EventPreviewDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 5), 1);

        when(eventService.getRelatedEvents(eq(5L), eq(EventStatus.UPCOMING), any(Pageable.class)))
            .thenReturn(page);

        mockMvc.perform(get("/events/myEvents/relatedEvents")
            .param("status", "UPCOMING")
            .param("size", "5")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].status").value("UPCOMING"))
            .andExpect(jsonPath("$.size").value(5));
    }

    // ============================
    // DELETE /events/delete/{eventId}
    // ============================
    @Test
    void deleteEvent_noContent() throws Exception {
        doNothing().when(eventService).deleteEvent(100L, "test@example.com");

        mockMvc.perform(delete("/events/delete/{eventId}", 100L)
            .principal((Principal) () -> "test@example.com"))
            .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(100L, "test@example.com");
    }

    @Test
    void deleteEvent_badRequest() throws Exception {
        doThrow(new BadRequestException("Only organizer or admin can delete"))
            .when(eventService).deleteEvent(5L, "test@example.com");

        mockMvc.perform(delete("/events/delete/{eventId}", 5L)
            .principal((Principal) () -> "test@example.com")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    // ============================
    // PUT /events/{eventId}
    // ============================

    @Test
    void updateEvent_serviceBadRequest_shouldReturn400() throws Exception {
        String json = objectMapper.writeValueAsString(updReq);
        MockMultipartFile dto = new MockMultipartFile("event", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());

        when(eventService.update(eq(3L), any(UpdateEventDtoRequest.class), anyList(), anyString()))
            .thenThrow(new BadRequestException("Title must be between 1 and 70 characters"));

        mockMvc.perform(multipart("/events/{eventId}", 3L)
            .file(dto)
            .with(req -> {
                req.setMethod("PUT");
                return req;
            })
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(MediaType.APPLICATION_JSON)
            .principal((Principal) () -> "test@example.com"))
            .andExpect(status().isBadRequest());
    }

    // ============================
    // DELETE /events/removeAttender/{eventId}
    // ============================
    @Test
    void removeAttender_ok_returnsRemovedTrue() throws Exception {
        when(eventService.removeAttender(11L, mockUser)).thenReturn(true);

        mockMvc.perform(delete("/events/removeAttender/{id}", 11L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.removed").value(true));

        verify(eventService).removeAttender(11L, mockUser);
    }

    @Test
    void removeAttender_notFound_404() throws Exception {
        when(eventService.removeAttender(99L, mockUser))
                .thenThrow(new NotFoundException("Event doesn't exist by this id: 99"));

        mockMvc.perform(delete("/events/removeAttender/{id}", 99L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ============================
    // POST /events/addAttender/{eventId}
    // ============================
    @Test
    void addAttender_ok_returnsAddedTrue() throws Exception {
        when(eventService.addAttender(12L, mockUser)).thenReturn(true);

        mockMvc.perform(post("/events/addAttender/{id}", 12L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added").value(true));

        verify(eventService).addAttender(12L, mockUser);
    }

    @Test
    void addAttender_eventNotFound_404() throws Exception {
        when(eventService.addAttender(404L, mockUser))
                .thenThrow(new NotFoundException("Event doesn't exist by this id: 404"));

        mockMvc.perform(post("/events/addAttender/{id}", 404L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
