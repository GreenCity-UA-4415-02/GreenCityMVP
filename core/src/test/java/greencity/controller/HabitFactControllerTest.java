package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.PageableDto;
import greencity.dto.habitfact.HabitFactDtoResponse;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habitfact.HabitFactUpdateDto;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.language.LanguageDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitFactService;
import greencity.service.LanguageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// ВАЖЛИВО: Імпортуємо для використання валідатора у standalone setup
import org.springframework.validation.Validator;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitFactControllerTest {

    @Mock
    private HabitFactService habitFactService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ErrorAttributes errorAttributes;
    @Mock
    private LanguageService languageService;
    @Mock
    private ModelMapper mapper;
    @InjectMocks
    private HabitFactController habitFactController;
    private MockMvc mockMvc;

    private static final String HABIT_LINK = "/facts";
    private static final Long HABIT_ID = 2L;
    private static final UserVO TEST_USER_VO;
    static {
        TEST_USER_VO = new UserVO();
        TEST_USER_VO.setId(2L);
        TEST_USER_VO.setEmail("test@gmail.com");
    }

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(languageService.findAllLanguageCodes()).thenReturn(List.of("en", "ua"));

        Mockito.lenient().when(errorAttributes.getErrorAttributes(any(), any()))
            .thenReturn(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "error", "Bad Request",
                "message", "Error message",
                "path", HABIT_LINK));

        this.mockMvc = MockMvcBuilders.standaloneSetup(habitFactController)
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(new Validator() {
                @Override
                public boolean supports(Class<?> clazz) {
                    return true;
                }

                @Override
                public void validate(Object target, org.springframework.validation.Errors errors) {
                }
            })
            .build();
    }

    @Test
    @DisplayName("Test getRandomFactByHabitId should return 200 OK")
    void getRandomFactByHabitIdStatus200() throws Exception {
        LanguageTranslationDTO expectedDto = LanguageTranslationDTO.builder()
            .language(LanguageDTO.builder().code("en").build())
            .build();

        when(habitFactService.getRandomHabitFactByHabitIdAndLanguage(eq(HABIT_ID), eq("en"))).thenReturn(expectedDto);

        mockMvc.perform(get(HABIT_LINK + "/random/{habitId}", HABIT_ID)
            .param("language", "en")
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(habitFactService).getRandomHabitFactByHabitIdAndLanguage(eq(HABIT_ID), eq("en"));
    }

    @Test
    @DisplayName("Test getHabitFactOfTheDay should return 200 OK")
    void getHabitFactOfTheDayStatus200() throws Exception {
        Long languageId = 2L;
        LanguageTranslationDTO expectedDto = LanguageTranslationDTO.builder()
            .language(LanguageDTO.builder().code("en").build())
            .build();
        when(habitFactService.getHabitFactOfTheDay(languageId)).thenReturn(expectedDto);
        mockMvc.perform(get(HABIT_LINK + "/dayFact/{languageId}", languageId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(habitFactService).getHabitFactOfTheDay(languageId);
    }

    @Test
    @DisplayName("Test getAll should return 200 OK")
    void getAllStatus200() throws Exception {
        LanguageTranslationDTO factDto = LanguageTranslationDTO.builder()
            .content("Test Fact Content")
            .language(LanguageDTO.builder().code("en").build())
            .build();
        PageableDto<LanguageTranslationDTO> expectedPage = new PageableDto<>(
            Collections.singletonList(factDto),
            1L, 0, 1);
        when(habitFactService.getAllHabitFacts(any(Pageable.class), eq("en"))).thenReturn(expectedPage);

        mockMvc.perform(get(HABIT_LINK)
            .param("page", "0")
            .param("size", "10")
            .param("language", "en")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.page[0].content").value("Test Fact Content"));
        verify(habitFactService).getAllHabitFacts(any(Pageable.class), eq("en"));
    }

    @Test
    @DisplayName("Test save should return 201 CREATED")
    void saveStatus200() throws Exception {
        HabitFactPostDto postDto = HabitFactPostDto.builder().build();

        HabitFactVO expectedDto = HabitFactVO.builder()
            .id(1L)
            .build();
        HabitFactDtoResponse responseDto = HabitFactDtoResponse.builder()
            .id(1L)
            .build();

        when(habitFactService.save(any(HabitFactPostDto.class))).thenReturn(expectedDto);
        when(mapper.map(eq(expectedDto), eq(HabitFactDtoResponse.class))).thenReturn(responseDto);

        mockMvc.perform(post(HABIT_LINK)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(asJsonString(postDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L));
        verify(habitFactService).save(any(HabitFactPostDto.class));
        verify(mapper).map(eq(expectedDto), eq(HabitFactDtoResponse.class));
    }

    @Test
    @DisplayName("Test update should return 200 OK")
    void updateStatus200() throws Exception {
        Long idToUpdate = 5L;
        HabitFactUpdateDto updateDto = new HabitFactUpdateDto();

        HabitFactVO updatedHabitFactVO = HabitFactVO.builder().id(idToUpdate).build();

        HabitFactPostDto expectedResponseDto = HabitFactPostDto.builder().build();
        when(habitFactService.update(any(HabitFactUpdateDto.class), eq(idToUpdate)))
            .thenReturn(updatedHabitFactVO);
        when(mapper.map(eq(updatedHabitFactVO), eq(HabitFactPostDto.class)))
            .thenReturn(expectedResponseDto);

        mockMvc.perform(put(HABIT_LINK + "/{id}", idToUpdate)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(asJsonString(updateDto)))
            .andExpect(status().isOk());
        verify(habitFactService).update(any(HabitFactUpdateDto.class), eq(idToUpdate));
        verify(mapper).map(eq(updatedHabitFactVO), eq(HabitFactPostDto.class));
    }

    @Test
    @DisplayName("Test delete should return 200 OK")
    void deleteStatus200() throws Exception {
        when(habitFactService.delete(HABIT_ID)).thenReturn(2L);
        mockMvc.perform(delete(HABIT_LINK + "/{id}", HABIT_ID)
                       .accept(MediaType.APPLICATION_JSON_VALUE)
                       .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk());
        verify(habitFactService).delete(HABIT_ID);
    }

    @Test
    @DisplayName("Test delete should return 400 BAD REQUEST")
    void deleteStatus400() throws Exception {
        Long nonExistentId = HABIT_ID + 999;

        when(habitFactService.delete(nonExistentId)).thenThrow(new BadRequestException("Bad request!"));

        mockMvc.perform(delete(HABIT_LINK + "/{id}", nonExistentId)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertInstanceOf(
                BadRequestException.class,
                result.getResolvedException()));
        verify(habitFactService).delete(nonExistentId);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}