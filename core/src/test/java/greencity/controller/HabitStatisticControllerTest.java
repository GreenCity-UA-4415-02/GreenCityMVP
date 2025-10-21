package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import greencity.annotations.CurrentUser;
import greencity.dto.habitstatistic.*;
import greencity.dto.user.UserVO;
import greencity.enums.HabitRate;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitStatisticService;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitStatisticControllerTest {

    @Mock
    private HabitStatisticService habitStatisticService;
    @Mock
    private ObjectMapper objectMapper;

    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @InjectMocks
    private HabitStatisticController habitStatisticController;
    private MockMvc mockMvc;

    private static final String HABIT_LINK = "/habit/statistic";
    private static final Long HABIT_ID = 102L;
    private static final UserVO TEST_USER_VO;
    static {
        TEST_USER_VO = new UserVO();
        TEST_USER_VO.setId(2L);
        TEST_USER_VO.setEmail("test@gmail.com");
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitStatisticController)
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new CurrentUserHandler())
            .build();
    }

    @Test
    @DisplayName("Test findAllByHabitId should return 200 OK")
    void findAllByHabitIdStatus200() throws Exception {
        GetHabitStatisticDto expectedDto = GetHabitStatisticDto.builder()
            .habitStatisticDtoList(Collections.emptyList())
            .amountOfUsersAcquired(1L)
            .build();

        when(habitStatisticService.findAllStatsByHabitId(HABIT_ID)).thenReturn(expectedDto);

        mockMvc.perform(get(HABIT_LINK + "/{habitId}", HABIT_ID)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(habitStatisticService).findAllStatsByHabitId(HABIT_ID);
    }

    @Test
    @DisplayName("Test findAllStatsByHabitAssignId should return 200 OK")
    void findAllStatsByHabitAssignIdStatus200() throws Exception {
        List<HabitStatisticDto> exceptedDtoList = List.of(HabitStatisticDto.builder().id(1L).build());

        when(habitStatisticService.findAllStatsByHabitAssignId(HABIT_ID)).thenReturn(exceptedDtoList);

        mockMvc.perform(get(HABIT_LINK + "/assign/{habitAssignId}", HABIT_ID)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(habitStatisticService).findAllStatsByHabitAssignId(HABIT_ID);
    }

    @Test
    @DisplayName("Test saveHabitStatistic should return 200 OK")
    void saveHabitStatisticStatus200() throws Exception {
        AddHabitStatisticDto habitStatisticDto = AddHabitStatisticDto.builder()
            .amountOfItems(5)
            .habitRate(HabitRate.DEFAULT)
            .createDate(ZonedDateTime.now())
            .build();

        HabitStatisticDto exceptedDto = HabitStatisticDto.builder()
            .id(1L)
            .createDate(ZonedDateTime.now())
            .habitAssignId(HABIT_ID)
            .habitRate(HabitRate.DEFAULT)
            .amountOfItems(5)
            .build();

        when(habitStatisticService.saveByHabitIdAndUserId(eq(HABIT_ID), eq(TEST_USER_VO.getId()),
            any(AddHabitStatisticDto.class))).thenReturn(exceptedDto);

        mockMvc.perform(post(HABIT_LINK + "/{habitId}", HABIT_ID)
            .content(asJsonString(habitStatisticDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
        verify(habitStatisticService).saveByHabitIdAndUserId(eq(HABIT_ID), eq(TEST_USER_VO.getId()),
            any(AddHabitStatisticDto.class));
    }

    @Test
    @DisplayName("Test updateStatistic should return 200 OK")
    void updateStatisticStatus200() throws Exception {
        UpdateHabitStatisticDto habitStatisticForUpdateDto =
            UpdateHabitStatisticDto.builder().amountOfItems(5).habitRate(HabitRate.DEFAULT).build();
        UpdateHabitStatisticDto expectedDto =
            UpdateHabitStatisticDto.builder().amountOfItems(5).habitRate(HabitRate.DEFAULT).build();

        when(habitStatisticService.update(HABIT_ID, TEST_USER_VO.getId(), habitStatisticForUpdateDto))
            .thenReturn(expectedDto);

        mockMvc.perform(put(HABIT_LINK + "/{id}", HABIT_ID)
            .content(asJsonString(habitStatisticForUpdateDto))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
        verify(habitStatisticService).update(HABIT_ID, TEST_USER_VO.getId(), habitStatisticForUpdateDto);
    }

    @Test
    @DisplayName("Test getTodayStatisticsForAllHabitItems should return 200 OK")
    void getTodayStatisticsForAllHabitItemsStatus200() throws Exception {
        List<HabitItemsAmountStatisticDto> exeptedDtoList = List.of(
            HabitItemsAmountStatisticDto.builder().habitItem("1").notTakenItems(1L).build(),
            HabitItemsAmountStatisticDto.builder().habitItem("2").notTakenItems(2L).build());
        when(habitStatisticService.getTodayStatisticsForAllHabitItems("en")).thenReturn(exeptedDtoList);

        mockMvc.perform(get(HABIT_LINK + "/todayStatisticsForAllHabitItems")
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$.length()").value(exeptedDtoList.size()))
            .andExpect(jsonPath("$[0].habitItem").isNotEmpty());
        verify(habitStatisticService).getTodayStatisticsForAllHabitItems("en");
    }

    @Test
    @DisplayName("Test findAmountOfAcquiredHabits should return 200 OK")
    void findAmountOfAcquiredHabitsStatus200() throws Exception {

        when(habitStatisticService.getAmountOfAcquiredHabitsByUserId(TEST_USER_VO.getId())).thenReturn(5L);

        mockMvc.perform(get(HABIT_LINK + "/acquired/count")
                       .param("userId", String.valueOf(TEST_USER_VO.getId()))
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .accept(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk());
        verify(habitStatisticService).getAmountOfAcquiredHabitsByUserId(TEST_USER_VO.getId());
    }

    @Test
    @DisplayName("Test findAmountOfHabitsInProgress should return 200 OK")
    void findAmountOfHabitsInProgressStatus200() throws Exception{
        when(habitStatisticService.getAmountOfHabitsInProgressByUserId(TEST_USER_VO.getId())).thenReturn(5L);

        mockMvc.perform(get(HABIT_LINK + "/in-progress/count")
                       .param("userId", String.valueOf(TEST_USER_VO.getId()))
                       .contentType(MediaType.APPLICATION_JSON_VALUE)
                       .accept(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk());
        verify(habitStatisticService).getAmountOfHabitsInProgressByUserId(TEST_USER_VO.getId());
    }

    private static class CurrentUserHandler implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(@NotNull MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class)
                && UserVO.class.isAssignableFrom(parameter.getParameterType());

        }

        @Override
        public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            TEST_USER_VO.setId(2L);
            TEST_USER_VO.setEmail("test@gmail.com");
            return TEST_USER_VO;
        }
    }

    private String asJsonString(final Object obj) {
        try {
            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}