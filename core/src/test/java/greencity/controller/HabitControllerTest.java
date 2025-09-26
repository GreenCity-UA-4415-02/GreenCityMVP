package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.annotations.CurrentUser;

import greencity.dto.PageableDto;
import greencity.dto.habit.AddCustomHabitDtoRequest;
import greencity.dto.habit.AddCustomHabitDtoResponse;
import greencity.dto.habit.HabitDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitService;
import greencity.service.TagsService;
import jakarta.servlet.ServletException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitControllerTest {
    @Mock
    private HabitService habitService;
    @Mock
    private TagsService tagsService;
    @Mock
    private ObjectMapper objectMapper;

    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();
    @InjectMocks
    private HabitController habitController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitController)
                                      .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                                      .setCustomArgumentResolvers(
                                              new PageableHandlerMethodArgumentResolver(),
                                              new CurrentUserHandler())
                                      .build() ;
    }

    private static class CurrentUserHandler implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter != null
                    && parameter.hasParameterAnnotation(CurrentUser.class)
                    && UserVO.class.isAssignableFrom(parameter.getParameterType());

        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            UserVO userVO = new UserVO();
            userVO.setId(1L);
            userVO.setEmail("test@gmail.com");
            return userVO;
        }
    }

    @Test
    @DisplayName("Test getHabitById should return 200 OK")
    void getHabitById_validId_returnOk() throws Exception {
        Long habitId = 1L;
        Locale locale = new Locale("en");
        HabitDto exceptedDto = new HabitDto();
        exceptedDto.setId(habitId);

        when(habitService.getByIdAndLanguageCode(habitId,locale.getLanguage())).thenReturn(exceptedDto);

        mockMvc.perform(get("/habit/{id}", habitId)
                       .param("lang", "en")
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andDo(print())
               .andExpect(jsonPath("$.id").value(habitId));
        verify(habitService).getByIdAndLanguageCode(habitId, "en");

    }

    @Test
    @DisplayName("Test getAllHabits should return 200 OK")
    void getAllHabits_returnsOk() throws Exception {
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setEmail("test@gmail.com");
        new Locale("en");
        Pageable.unpaged();
        PageableDto<HabitDto> pageableDto = new PageableDto<>(Collections.singletonList(new HabitDto()), 1, 1, 1);

        when(habitService.getAllHabitsByLanguageCode(any(UserVO.class), any(Pageable.class), anyString())).thenReturn(pageableDto);

        mockMvc.perform(get("/habit")
                       .param("lang", "en")
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
        verify(habitService).getAllHabitsByLanguageCode(any(UserVO.class), any(Pageable.class), anyString());
    }
    @Test
    @DisplayName("Test getShoppingListItems should return 200 OK")
    void getShoppingListItems_returnsOk() throws Exception {
        Long habitId = 1L;
        Locale locale = new Locale("en");
        List<ShoppingListItemDto> shoppingList = Collections.singletonList(new ShoppingListItemDto());

        when(habitService.getShoppingListForHabit(habitId, locale.getLanguage())).thenReturn(shoppingList);

        mockMvc.perform(get("/habit/{id}/shopping-list", habitId)
                       .param("lang", "en")
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());

        verify(habitService).getShoppingListForHabit(habitId, locale.getLanguage());
    }

    @Test
    @DisplayName("Test getAllByTagsAndLanguageCode should return 200 OK")
    void getAllByTagsAndLanguageCode_returnsOk() throws Exception {
        new Locale("en");
        Collections.singletonList("test");
        Pageable.unpaged();
        PageableDto<HabitDto> pageableDto = new PageableDto<>(Collections.singletonList(new HabitDto()), 1, 1, 1);

        when(habitService.getAllByTagsAndLanguageCode(any(Pageable.class), any(), anyString())).thenReturn(pageableDto);

        mockMvc.perform(get("/habit/tags/search")
                       .param("tags", "test")
                       .param("lang", "en")
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());

        verify(habitService).getAllByTagsAndLanguageCode(any(Pageable.class), any(), anyString());
    }

    @Test
    @DisplayName("Test getAllByDifferentParameters should return 200 OK")
    void getAllByDifferentParameters_returnsOk() throws Exception {
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setEmail("test@example.com");
        new Locale("en");
        Pageable.unpaged();
        PageableDto<HabitDto> pageableDto = new PageableDto<>(Collections.singletonList(new HabitDto()), 1, 1, 1);

        when(habitService.getAllByDifferentParameters(any(UserVO.class), any(Pageable.class), any(), any(), any(), anyString())).thenReturn(pageableDto);

        mockMvc.perform(get("/habit/search")
                       .param("tags", "test")
                       .param("lang", "en")
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());

        verify(habitService).getAllByDifferentParameters(any(UserVO.class), any(Pageable.class), any(), any(), any(), anyString());
    }

    @Test
    @DisplayName("Test getAllByDifferentParameters without params should return 400 Bad Request")
    void getAllByDifferentParameters_withoutParams_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/habit/search")
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(result -> assertInstanceOf(BadRequestException.class, result.getResolvedException()));
    }

    @Test
    @DisplayName("Test findAllHabitsTags should return 200 OK")
    void findAllHabitsTags_returnsOk() throws Exception {
        Locale locale = new Locale("en");
        List<String> tags = Collections.singletonList("test-tag");

        when(tagsService.findAllHabitsTags(locale.getLanguage())).thenReturn(tags);

        mockMvc.perform(get("/habit/tags")
                       .param("lang", "en")
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0]").value("test-tag"));

        verify(tagsService).findAllHabitsTags(locale.getLanguage());
    }


    @Test
    @DisplayName("Test addCustomHabit should return 201 Created")
    void addCustomHabit_returnsCreated() throws Exception {

        Principal principal = () -> "test@example.com";
        AddCustomHabitDtoRequest requestDto = new AddCustomHabitDtoRequest();

        requestDto.setComplexity(2);
        requestDto.setDefaultDuration(30);

        AddCustomHabitDtoResponse responseDto = new AddCustomHabitDtoResponse();
        MockMultipartFile image = new MockMultipartFile("image", "filename.jpg", "image/jpeg", "some-image".getBytes());
        MockPart requestPart = new MockPart("request", asJsonString(requestDto).getBytes());
        requestPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        when(habitService.addCustomHabit(any(AddCustomHabitDtoRequest.class), any(MultipartFile.class), anyString()))
                .thenReturn(responseDto);

        mockMvc.perform(multipart("/habit/custom")
                       .file(image)
                       .part(requestPart)
                       .principal(principal)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated());

        verify(habitService).addCustomHabit(any(AddCustomHabitDtoRequest.class), any(MultipartFile.class), anyString());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Test getFriendsAssignedToHabitProfilePictures should return 200 OK")
    void getFriendsAssignedToHabitProfilePictures_returnsOk() throws Exception {
        Long habitId = 1L;
        List<UserProfilePictureDto> profilePictures = Collections.singletonList(new UserProfilePictureDto());

        when(habitService.getFriendsAssignedToHabitProfilePictures(any(Long.class), any(Long.class))).thenReturn(profilePictures);

        mockMvc.perform(get("/habit/{habitId}/friends/profile-pictures", habitId)
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());

        verify(habitService).getFriendsAssignedToHabitProfilePictures(any(Long.class), any(Long.class));
    }
}