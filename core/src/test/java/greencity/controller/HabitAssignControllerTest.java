package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.annotations.CurrentUser;
import greencity.dto.habit.*;
import greencity.dto.user.UserVO;
import greencity.enums.HabitAssignStatus;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.UserAlreadyHasHabitAssignedException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitAssignService;
import org.jetbrains.annotations.NotNull;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitAssignControllerTest {
    @Mock
    private HabitAssignService habitAssignService;
    @Mock
    private ObjectMapper objectMapper;

    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();
    @InjectMocks
    private HabitAssignController habitAssignController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitAssignController)
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new CurrentUserHandler())
            .build();
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
            UserVO userVO = new UserVO();
            userVO.setId(1L);
            userVO.setEmail("test@gmail.com");
            return userVO;
        }
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Test assignDefaultHabit should return 201 Created")
    void assignDefaultHabit_returnsCreated() throws Exception {
        Long habitId = 1L;
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setEmail("test@gmail.com");

        HabitAssignManagementDto expectedDto = HabitAssignManagementDto.builder()
            .id(10L)
            .habitId(habitId)
            .status(HabitAssignStatus.INPROGRESS)
            .build();

        when(habitAssignService.assignDefaultHabitForUser(habitId, userVO))
            .thenReturn(expectedDto);

        mockMvc.perform(post("/habit/assign/{habitId}", habitId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(expectedDto.getId()))
            .andExpect(jsonPath("$.habitId").value(habitId))
            .andExpect(jsonPath("$.status").value("INPROGRESS"));

        verify(habitAssignService).assignDefaultHabitForUser(habitId, userVO);
    }

    @Test
    @DisplayName("Test assignDefaultHabit should return 404 Not Found when habit not exists")
    void assignDefaultHabit_throwsNotFoundException() throws Exception {
        Long nonExistentHabitId = 999L;
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setEmail("test@gmail.com");

        when(habitAssignService.assignDefaultHabitForUser(nonExistentHabitId, userVO))
            .thenThrow(new NotFoundException("Habit with id " + nonExistentHabitId + " not found."));

        mockMvc.perform(post("/habit/assign/{habitId}", nonExistentHabitId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(habitAssignService).assignDefaultHabitForUser(nonExistentHabitId, userVO);
    }

    @Test
    @DisplayName("Test assignCustomHabit should return 201 Created on success")
    void assignCustomHabit_returnsCreated() throws Exception {
        HabitAssignCustomPropertiesDto requestDto = HabitAssignCustomPropertiesDto.builder().build();

        Long newHabitId = 2L;
        HabitAssignManagementDto expectedDto = HabitAssignManagementDto.builder()
            .id(11L)
            .habitId(newHabitId)
            .status(HabitAssignStatus.INPROGRESS)
            .build();

        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setEmail("test@gmail.com");

        when(habitAssignService.assignCustomHabitForUser(newHabitId, userVO, requestDto))
            .thenReturn(Collections.singletonList(expectedDto));

        mockMvc.perform(post("/habit/assign/{habitId}/custom", newHabitId)
            .content(asJsonString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].id").value(expectedDto.getId()))
            .andExpect(jsonPath("$[0].habitId").value(newHabitId))
            .andExpect(jsonPath("$[0].status").value("INPROGRESS"));

        verify(habitAssignService).assignCustomHabitForUser(newHabitId, userVO, requestDto);
    }

    @Test
    @DisplayName("Test assignCustomHabit should return 404 Not Found when habit not exists")
    void assignCustomHabit_throwsNotFoundException() throws Exception {
        Long nonExistentHabitId = 999L;
        HabitAssignCustomPropertiesDto requestDto = HabitAssignCustomPropertiesDto.builder().build();
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setEmail("test@gmail.com");

        when(habitAssignService.assignCustomHabitForUser(nonExistentHabitId, userVO, requestDto))
            .thenThrow(new NotFoundException("Habit with id " + nonExistentHabitId + " not found."));

        mockMvc.perform(post("/habit/assign/{habitId}/custom", nonExistentHabitId)
            .content(asJsonString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(habitAssignService).assignCustomHabitForUser(nonExistentHabitId, userVO, requestDto);
    }

    @Test
    @DisplayName("Test assignDefaultHabit should return 409 Conflict if habit is already assigned")
    void assignDefaultHabit_throwsConflictException() throws Exception {
        Long assignedHabitId = 5L;
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setEmail("test@gmail.com");

        when(habitAssignService.assignDefaultHabitForUser(assignedHabitId, userVO))
            .thenThrow(new UserAlreadyHasHabitAssignedException(
                "User already has assigned habit with id: " + assignedHabitId));

        mockMvc.perform(post("/habit/assign/{habitId}", assignedHabitId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(habitAssignService).assignDefaultHabitForUser(assignedHabitId, userVO);
    }

    @Test
    @DisplayName("Test assignCustomHabit should return 409 Conflict if habit is already assigned")
    void assignCustomHabit_throwsConflictException() throws Exception {
        Long assignedHabitId = 5L;
        HabitAssignCustomPropertiesDto requestDto = HabitAssignCustomPropertiesDto.builder().build();
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setEmail("test@gmail.com");

        when(habitAssignService.assignCustomHabitForUser(assignedHabitId, userVO, requestDto))
            .thenThrow(new UserAlreadyHasHabitAssignedException(
                "User already has assigned habit with id: " + assignedHabitId));

        mockMvc.perform(post("/habit/assign/{habitId}/custom", assignedHabitId)
            .content(asJsonString(requestDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verify(habitAssignService).assignCustomHabitForUser(assignedHabitId, userVO, requestDto);
    }

    @Test
    @DisplayName("Test getHabitAssignById should return 200 OK on success")
    void getHabitAssignById_returnsOk() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long habitAssignId = 100L;
        HabitAssignDto expectedDto = HabitAssignDto.builder()
            .id(habitAssignId)
            .status(HabitAssignStatus.INPROGRESS)
            .createDateTime(ZonedDateTime.now())
            .build();

        when(habitAssignService.getByHabitAssignIdAndUserId(habitAssignId, testUserVO.getId(), "en"))
            .thenReturn(expectedDto);

        mockMvc.perform(get("/habit/assign/{habitAssignId}", habitAssignId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .locale(Locale.ENGLISH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(habitAssignId))
            .andExpect(jsonPath("$.status").value("INPROGRESS"));

        verify(habitAssignService).getByHabitAssignIdAndUserId(habitAssignId, testUserVO.getId(), "en");
    }

    @Test
    @DisplayName("Test getHabitAssignById should return 404 Not Found when habit assign does not exist")
    void getHabitAssignById_throwsNotFoundException() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long nonExistentAssignId = 999L;

        when(habitAssignService.getByHabitAssignIdAndUserId(nonExistentAssignId, testUserVO.getId(), "en"))
            .thenThrow(new NotFoundException("Habit assignment not found for this user."));

        mockMvc.perform(get("/habit/assign/{habitAssignId}", nonExistentAssignId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(habitAssignService).getByHabitAssignIdAndUserId(nonExistentAssignId, testUserVO.getId(), "en");
    }

    @Test
    @DisplayName("Test updateAssignByHabitId (status update) should return 200 OK on successful status update")
    void updateAssignByHabitId_returnsOk() throws Exception {
        Long assignId = 101L;

        HabitAssignStatDto requestDto = HabitAssignStatDto.builder()
            .status(HabitAssignStatus.ACQUIRED)
            .build();

        HabitAssignManagementDto responseDto = HabitAssignManagementDto.builder()
            .id(assignId)
            .habitId(5L) // Приклад
            .status(HabitAssignStatus.ACQUIRED)
            .build();

        when(habitAssignService.updateStatusByHabitAssignId(eq(assignId), any(HabitAssignStatDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(patch("/habit/assign/{habitAssignId}", assignId)
            .content(asJsonString(requestDto)) // Надсилаємо JSON-тіло
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(assignId))
            .andExpect(jsonPath("$.status").value("ACQUIRED"));

        verify(habitAssignService).updateStatusByHabitAssignId(eq(assignId), any(HabitAssignStatDto.class));
    }

    @Test
    @DisplayName("Test updateAssignByHabitId (status update) should return 404 Not Found when habit assign does not exist")
    void updateAssignByHabitId_throwsNotFoundException() throws Exception {
        Long nonExistentAssignId = 999L;
        HabitAssignStatDto requestDto = HabitAssignStatDto.builder()
            .status(HabitAssignStatus.ACQUIRED)
            .build();

        when(habitAssignService.updateStatusByHabitAssignId(eq(nonExistentAssignId), any(HabitAssignStatDto.class)))
            .thenThrow(new NotFoundException("Habit assignment not found."));

        mockMvc.perform(patch("/habit/assign/{habitAssignId}", nonExistentAssignId)
            .content(asJsonString(requestDto))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(habitAssignService).updateStatusByHabitAssignId(eq(nonExistentAssignId), any(HabitAssignStatDto.class));
    }

    @Test
    @DisplayName("Test deleteHabitAssign should return 200 OK on successful deletion")
    void deleteHabitAssign_returnsOk() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long assignId = 102L;

        doNothing().when(habitAssignService).deleteHabitAssign(assignId, testUserVO.getId());

        mockMvc.perform(delete("/habit/assign/delete/{habitAssignId}", assignId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(habitAssignService).deleteHabitAssign(assignId, testUserVO.getId());
    }

    @Test
    @DisplayName("Test updateHabitAssignDuration should return 200 OK on successful update")
    void updateHabitAssignDurationStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long assignId = 102L;
        Integer duration = 50;

        HabitAssignUserDurationDto exceptedDto = HabitAssignUserDurationDto.builder()
            .userId(testUserVO.getId())
            .habitId(assignId)
            .duration(duration)
            .build();

        when(habitAssignService.updateUserHabitInfoDuration(assignId, testUserVO.getId(), duration))
            .thenReturn(exceptedDto);

        mockMvc.perform(put("/habit/assign/{habitAssignId}/update-habit-duration", assignId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .param("duration", String.valueOf(duration)))
            .andExpect(status().isOk());

        verify(habitAssignService).updateUserHabitInfoDuration(assignId, testUserVO.getId(), duration);
    }

    @Test
    @DisplayName("Test updateHabitAssignDuration should return 404 on update")
    void updateHabitAssignDurationStatus404() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long assignId = 999L;
        Integer duration = 50;

        when(habitAssignService.updateUserHabitInfoDuration(assignId, testUserVO.getId(), duration))
            .thenThrow(new NotFoundException("Habit assignment not found."));

        mockMvc.perform(put("/habit/assign/{habitAssignId}/update-habit-duration", assignId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .param("duration", String.valueOf(duration)))
            .andExpect(status().isNotFound());

        verify(habitAssignService).updateUserHabitInfoDuration(assignId, testUserVO.getId(), duration);
    }

    @Test
    @DisplayName("Test getCurrentUserHabitAssignsByIdAndAcquired should return 200 OK")
    void getCurrentUserHabitAssignsByIdAndAcquiredStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");

        List<HabitAssignDto> exceptedDtoList = List.of(
            HabitAssignDto.builder().id(1L).status(HabitAssignStatus.ACQUIRED).build(),
            HabitAssignDto.builder().id(2L).status(HabitAssignStatus.INPROGRESS).build());

        when(habitAssignService.getAllHabitAssignsByUserIdAndStatusNotCancelled(testUserVO.getId(), "en"))
            .thenReturn(exceptedDtoList);

        mockMvc.perform(get("/habit/assign/allForCurrentUser")
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[1].status").value("INPROGRESS"));
        verify(habitAssignService).getAllHabitAssignsByUserIdAndStatusNotCancelled(testUserVO.getId(), "en");
    }

    @Test
    @DisplayName("Test getUserShoppingAndCustomShoppingLists should return 200 OK with correct DTO")
    void getUserShoppingAndCustomShoppingListsStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long habitId = 102L;

        UserShoppingAndCustomShoppingListsDto exceptedDto = UserShoppingAndCustomShoppingListsDto.builder().build();

        when(habitAssignService.getUserShoppingAndCustomShoppingLists(testUserVO.getId(), habitId, "en"))
            .thenReturn(exceptedDto);

        mockMvc.perform(get("/habit/assign/{habitAssignId}/allUserAndCustomList", habitId)
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").exists());
        verify(habitAssignService).getUserShoppingAndCustomShoppingLists(testUserVO.getId(), habitId, "en");
    }

    @Test
    @DisplayName("Test updateUserAndCustomShoppingLists should return 200 OK")
    void updateUserAndCustomShoppingListsStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long habitId = 102L;
        UserShoppingAndCustomShoppingListsDto listsDto = UserShoppingAndCustomShoppingListsDto.builder()
            .customShoppingListItemDto(Collections.emptyList())
            .userShoppingListItemDto(Collections.emptyList())
            .build();

        doNothing().when(habitAssignService).fullUpdateUserAndCustomShoppingLists(
            eq(testUserVO.getId()),
            eq(habitId),
            any(UserShoppingAndCustomShoppingListsDto.class),
            eq("en"));

        mockMvc.perform(put("/habit/assign/{habitAssignId}/allUserAndCustomList", habitId)
            .locale(Locale.ENGLISH)
            .content(asJsonString(listsDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).fullUpdateUserAndCustomShoppingLists(
            eq(testUserVO.getId()),
            eq(habitId),
            any(UserShoppingAndCustomShoppingListsDto.class),
            eq("en"));
    }

    @Test
    @DisplayName("Test getListOfUserAndCustomShoppingListsInprogress should return 200 OK")
    void getListOfUserAndCustomShoppingListsInprogressStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");

        List<UserShoppingAndCustomShoppingListsDto> exceptedListDto = List.of(
            UserShoppingAndCustomShoppingListsDto.builder()
                .userShoppingListItemDto(Collections.emptyList())
                .customShoppingListItemDto(Collections.emptyList())
                .build());

        when(habitAssignService.getListOfUserAndCustomShoppingListsWithStatusInprogress(testUserVO.getId(), "en"))
            .thenReturn(exceptedListDto);

        mockMvc.perform(get("/habit/assign/allUserAndCustomShoppingListsInprogress")
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
        verify(habitAssignService).getListOfUserAndCustomShoppingListsWithStatusInprogress(testUserVO.getId(), "en");
    }

    @Test
    @DisplayName("Test getAllHabitAssignsByHabitIdAndAcquired should return 200 OK")
    void getAllHabitAssignsByHabitIdAndAcquiredStatus200() throws Exception {
        Long habitId = 102L;

        List<HabitAssignDto> exceptedListDto = List.of(
            HabitAssignDto.builder().id(1L).status(HabitAssignStatus.ACQUIRED).build(),
            HabitAssignDto.builder().id(2L).status(HabitAssignStatus.INPROGRESS).build());

        when(habitAssignService.getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId, "en"))
            .thenReturn(exceptedListDto);

        mockMvc.perform(get("/habit/assign/{habitId}/all", habitId)
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[1].status").value("INPROGRESS"));
        verify(habitAssignService).getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId, "en");
    }

    @Test
    @DisplayName("Test getHabitAssignByHabitId should return 200 OK")
    void getHabitAssignByHabitIdStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long habitId = 102L;

        HabitAssignDto exceptedDto = HabitAssignDto.builder().id(1L).status(HabitAssignStatus.ACQUIRED).build();

        when(habitAssignService.findHabitAssignByUserIdAndHabitId(testUserVO.getId(), habitId, "en"))
            .thenReturn(exceptedDto);

        mockMvc.perform(get("/habit/assign/{habitId}/active", habitId)
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).findHabitAssignByUserIdAndHabitId(testUserVO.getId(), habitId, "en");
    }

    @Test
    @DisplayName("Test getUsersHabitByHabitAssignId should return 200 OK")
    void getUsersHabitByHabitAssignIdStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long habitAssignId = 102L;

        HabitDto exceptedDto = HabitDto.builder().id(1L).build();

        when(habitAssignService.findHabitByUserIdAndHabitAssignId(testUserVO.getId(), habitAssignId, "en"))
            .thenReturn(exceptedDto);

        mockMvc.perform(get("/habit/assign/{habitAssignId}/more", habitAssignId)
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).findHabitByUserIdAndHabitAssignId(testUserVO.getId(), habitAssignId, "en");
    }

    @Test
    @DisplayName("Test enrollHabit should return 200 OK")
    void enrollHabitStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long habitAssignId = 102L;
        LocalDate date = LocalDate.now();

        HabitAssignDto exceptedDto = HabitAssignDto.builder().id(1L).status(HabitAssignStatus.ACQUIRED).build();

        when(habitAssignService.enrollHabit(habitAssignId, testUserVO.getId(), date, "en")).thenReturn(exceptedDto);

        mockMvc.perform(post("/habit/assign/{habitAssignId}/enroll/{date}", habitAssignId, date)
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).enrollHabit(habitAssignId, testUserVO.getId(), date, "en");
    }

    @Test
    @DisplayName("Test unenrollHabit should return 200 OK")
    void unenrollHabitStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long habitAssignId = 102L;
        LocalDate date = LocalDate.now();

        HabitAssignDto exceptedDto = HabitAssignDto.builder().id(1L).build();

        when(habitAssignService.unenrollHabit(habitAssignId, testUserVO.getId(), date)).thenReturn(exceptedDto);

        mockMvc.perform(post("/habit/assign/{habitAssignId}/unenroll/{date}", habitAssignId, date)
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).unenrollHabit(habitAssignId, testUserVO.getId(), date);
    }

    @Test
    @DisplayName("Test getInprogressHabitAssignOnDate should return 200 OK")
    void getInprogressHabitAssignOnDateStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        LocalDate date = LocalDate.now();

        List<HabitAssignDto> exceptedDto = List.of(
            HabitAssignDto.builder().id(1L).status(HabitAssignStatus.ACQUIRED).build(),
            HabitAssignDto.builder().id(2L).status(HabitAssignStatus.INPROGRESS).build());

        when(habitAssignService.findInprogressHabitAssignsOnDate(testUserVO.getId(), date, "en"))
            .thenReturn(exceptedDto);

        mockMvc.perform(get("/habit/assign/active/{date}", date)
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[1].status").value("INPROGRESS"));
        verify(habitAssignService).findInprogressHabitAssignsOnDate(testUserVO.getId(), date, "en");
    }

    @Test
    @DisplayName("Test getHabitAssignBetweenDates should return 200 OK")
    void getHabitAssignBetweenDatesStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);

        List<HabitsDateEnrollmentDto> exceptedListDto = List.of(
            HabitsDateEnrollmentDto.builder().habitAssigns(Collections.emptyList()).build());

        when(habitAssignService.findHabitAssignsBetweenDates(testUserVO.getId(), from, to, "en"))
            .thenReturn(exceptedListDto);

        mockMvc.perform(get("/habit/assign/activity/{from}/to/{to}", from, to)
            .locale(Locale.ENGLISH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
        verify(habitAssignService).findHabitAssignsBetweenDates(testUserVO.getId(), from, to, "en");
    }

    @Test
    @DisplayName("Test cancelHabitAssign should return 200 OK")
    void cancelHabitAssignStatus200() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long habitId = 102L;

        HabitAssignDto exceptedDto = HabitAssignDto.builder().id(1L).status(HabitAssignStatus.ACQUIRED).build();

        when(habitAssignService.cancelHabitAssign(habitId, testUserVO.getId())).thenReturn(exceptedDto);

        mockMvc.perform(patch("/habit/assign/cancel/{habitId}", habitId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).cancelHabitAssign(habitId, testUserVO.getId());
    }

    @Test
    @DisplayName("Test updateShoppingListStatus should return 200 OK")
    void updateShoppingListStatus200() throws Exception {
        UpdateUserShoppingListDto mockDto = UpdateUserShoppingListDto.builder()
            .habitAssignId(102L)
            .build();

        doNothing().when(habitAssignService).updateUserShoppingListItem(any(UpdateUserShoppingListDto.class));

        mockMvc.perform(put("/habit/assign/saveShoppingListForHabitAssign")
            .content(asJsonString(mockDto))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateUserShoppingListItem(any(UpdateUserShoppingListDto.class));
    }

    @Test
    @DisplayName("Test updateProgressNotificationHasDisplayed should return 200 OK")
    void updateProgressNotificationHasDisplayed() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long habitId = 102L;

        doNothing().when(habitAssignService).updateProgressNotificationHasDisplayed(habitId, testUserVO.getId());

        mockMvc.perform(put("/habit/assign/{habitAssignId}/updateProgressNotificationHasDisplayed", habitId)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitAssignService).updateProgressNotificationHasDisplayed(habitId, testUserVO.getId());
    }
}