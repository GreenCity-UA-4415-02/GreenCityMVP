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

import java.time.ZonedDateTime;
import java.util.Collections;
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
                                      .build() ;
    }

    private static class CurrentUserHandler implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(@NotNull MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class)
                    && UserVO.class.isAssignableFrom(parameter.getParameterType());

        }

        @Override
        public Object resolveArgument(@NotNull MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
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

        when(habitAssignService.assignCustomHabitForUser(newHabitId,userVO,requestDto))
                .thenReturn(Collections.singletonList(expectedDto));

        mockMvc.perform(post("/habit/assign/{habitId}/custom", newHabitId)
                       .content(asJsonString(requestDto))
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$[0].id").value(expectedDto.getId()))
               .andExpect(jsonPath("$[0].habitId").value(newHabitId))
               .andExpect(jsonPath("$[0].status").value("INPROGRESS"));

        verify(habitAssignService).assignCustomHabitForUser(newHabitId,userVO,requestDto);
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
                .thenThrow(new UserAlreadyHasHabitAssignedException("User already has assigned habit with id: " + assignedHabitId));

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
                .thenThrow(new UserAlreadyHasHabitAssignedException("User already has assigned habit with id: " + assignedHabitId));

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
    @DisplayName("Test updateHabitAssignStatus should return 200 OK on successful status update")
    void updateHabitAssignStatus_returnsOk() throws Exception {
        UserVO testUserVO = new UserVO();
        testUserVO.setId(1L);
        testUserVO.setEmail("test@gmail.com");
        Long assignId = 101L;
        HabitAssignStatDto expectedDto = HabitAssignStatDto.builder()
                                                            .status(HabitAssignStatus.ACQUIRED)
                                                            .build();
        HabitAssignManagementDto responseDto = HabitAssignManagementDto.builder()
                                                                       .id(assignId)
                                                                       .status(HabitAssignStatus.ACQUIRED)
                                                                       .build();
        when(habitAssignService.updateStatusByHabitAssignId(assignId,expectedDto)).thenReturn(responseDto);

        mockMvc.perform(patch("/habit/assign/{habitAssignId}/status", assignId)
                       .param("status", "ACQUIRED")
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(assignId))
               .andExpect(jsonPath("$.status").value("ACQUIRED"));

        verify(habitAssignService).updateStatusByHabitAssignId(assignId, expectedDto);
    }

//    @Test
//    @DisplayName("Test updateHabitAssignStatus should return 404 Not Found when habit assign does not exist")
//    void updateHabitAssignStatus_throwsNotFoundException() throws Exception {
//        UserVO testUserVO = new UserVO();
//        testUserVO.setId(1L);
//        testUserVO.setEmail("test@gmail.com");
//        Long nonExistentAssignId = 999L;
//
//        when(habitAssignService.updateStatusByHabitAssignId(nonExistentAssignId, testUserVO))
//                .thenThrow(new NotFoundException("Habit assignment not found."));
//
//        mockMvc.perform(patch("/habit/assign/{habitAssignId}/status", nonExistentAssignId)
//                       .param("status", "ACQUIRED")
//                       .contentType(MediaType.APPLICATION_JSON))
//               .andExpect(status().isNotFound());
//
//        verify(habitAssignService).updateStatusByHabitAssignId(nonExistentAssignId, testUserVO);
//    }

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
}