package greencity.controller;

import greencity.annotations.CurrentUserId;
import greencity.config.SecurityConfig;
import greencity.dto.shoppinglistitem.BulkSaveCustomShoppingListItemDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemSaveRequestDto;
import greencity.enums.ShoppingListItemStatus;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.CustomShoppingListItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Map;

import static greencity.ModelUtils.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration
@Import(SecurityConfig.class)
class CustomShoppingListItemControllerTest {
    @Mock
    private CustomShoppingListItemService customShoppingListItemService;
    @InjectMocks
    private CustomShoppingListItemController customShoppingListItemController;
    private MockMvc mockMvc;

    private static final String customShoppingListItemControllerLink = "/custom/shopping-list-items";
    private static final Long USER_ID = 1L;
    private static final Long HABIT_ID = 2L;
    private static final Long HABIT_ASSIGN_ID = 3L;
    private static final Long ITEM_ID = 4L;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(customShoppingListItemController)
            .setControllerAdvice(new CustomExceptionHandler(new DefaultErrorAttributes(), getObjectMapper()))
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new CurrentUserIdHandler())
            .build();
    }

    private static class CurrentUserIdHandler implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUserId.class)
                && Long.class.isAssignableFrom(parameter.getParameterType());

        }

        @Override
        public Object resolveArgument(MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
            Map<String, String> uriVars = (Map<String, String>) webRequest.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            return Long.valueOf(uriVars.get("userId")) == USER_ID;
        }
    }

    @Test
    @DisplayName("Get all test → 200 OK and list of DTO")
    void getAllAvailable_ok() throws Exception {
        CustomShoppingListItemResponseDto dto = getCustomShoppingListItemResponseDto();
        when(customShoppingListItemService.findAllAvailableCustomShoppingListItems(eq(USER_ID), eq(HABIT_ID)))
            .thenReturn(List.of(dto));

        mockMvc.perform(get(customShoppingListItemControllerLink + "/{userId}/{habitId}", USER_ID, HABIT_ID)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].text").value("text"))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(customShoppingListItemService).findAllAvailableCustomShoppingListItems(USER_ID, HABIT_ID);
        verifyNoMoreInteractions(customShoppingListItemService);
    }

    @Test
    @DisplayName("Post save user custom shopping list items test → 201 Created and list of DTO")
    void saveUserCustomShoppingListItems() throws Exception {
        CustomShoppingListItemSaveRequestDto dtoListItemSaveRequest = CustomShoppingListItemSaveRequestDto.builder()
            .text("text")
            .build();
        BulkSaveCustomShoppingListItemDto dtoRequest =
            new BulkSaveCustomShoppingListItemDto(List.of(dtoListItemSaveRequest));
        CustomShoppingListItemResponseDto dtoResponse = getCustomShoppingListItemResponseDto();

        when(customShoppingListItemService.save(any(BulkSaveCustomShoppingListItemDto.class), eq(USER_ID),
            eq(HABIT_ASSIGN_ID)))
            .thenReturn(List.of(dtoResponse));

        mockMvc
            .perform(post(customShoppingListItemControllerLink + "/{userId}/{habitAssignId}/custom-shopping-list-items",
                USER_ID, HABIT_ASSIGN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getObjectMapper().writeValueAsString(dtoRequest))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].text").value("text"));

        verify(customShoppingListItemService).save(any(BulkSaveCustomShoppingListItemDto.class), eq(USER_ID),
            eq(HABIT_ASSIGN_ID));
    }

    @Test
    @DisplayName("Post save user custom shopping list items without body test → 400 Bad Request")
    void save_missingBody_badRequest() throws Exception {
        mockMvc
            .perform(post(customShoppingListItemControllerLink + "/{userId}/{habitAssignId}/custom-shopping-list-items",
                USER_ID, HABIT_ASSIGN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    @DisplayName("Patch update item status test → 200 OK and DTO")
    void updateStatus_ok() throws Exception {
        String status = "INPROGRESS";
        when(customShoppingListItemService.updateItemStatus(USER_ID, ITEM_ID, status))
            .thenReturn(getCustomShoppingListItemResponseDto());

        mockMvc.perform(patch(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID)
            .param("itemId", String.valueOf(ITEM_ID))
            .param("status", status)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(customShoppingListItemService).updateItemStatus(USER_ID, ITEM_ID, status);
    }

    @ParameterizedTest(name = "Patch update item with status \"{0}\" → 200 OK")
    @ValueSource(strings = {"ACTIVE", "DONE", "DISABLED", "INPROGRESS"})
    void updateStatus_allStatuses_ok(String status) throws Exception {
        CustomShoppingListItemResponseDto dto = CustomShoppingListItemResponseDto.builder()
            .id(1L)
            .status(ShoppingListItemStatus.valueOf(status))
            .text("text")
            .build();
        when(customShoppingListItemService.updateItemStatus(USER_ID, ITEM_ID, status)).thenReturn(dto);

        mockMvc.perform(patch(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID)
            .param("itemId", String.valueOf(ITEM_ID))
            .param("status", status)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(status));

        verify(customShoppingListItemService).updateItemStatus(USER_ID, ITEM_ID, status);
        clearInvocations(customShoppingListItemService);
    }

    @Test
    @DisplayName("Patch update item status without itemId → 400 Bad Request")
    void updateStatus_missingItemId_badRequest() throws Exception {
        mockMvc.perform(patch(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID)
            .param("status", "DONE"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    @DisplayName("Patch update item status without status → 400 Bad Request")
    void updateStatus_missingStatus_badRequest() throws Exception {
        mockMvc.perform(patch(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID)
            .param("itemId", "1"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    @DisplayName("Patch update item status to done → 200 OK without body")
    void markDone_ok() throws Exception {
        doNothing().when(customShoppingListItemService).updateItemStatusToDone(USER_ID, ITEM_ID);

        mockMvc.perform(patch(customShoppingListItemControllerLink + "/{userId}/done", USER_ID)
            .param("itemId", String.valueOf(ITEM_ID)))
            .andExpect(status().isOk())
            .andExpect(content().string(""));

        verify(customShoppingListItemService).updateItemStatusToDone(USER_ID, ITEM_ID);
    }

    @Test
    @DisplayName("Patch update item status to done → 400 Bad Request")
    void markDone_missingItemId_badRequest() throws Exception {
        mockMvc.perform(patch(customShoppingListItemControllerLink + "/{userId}/done", USER_ID))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    @DisplayName("Delete custom shopping list items → 200 OK and list of deleted IDs")
    void bulkDelete_ok() throws Exception {
        String ids = "1,2,3";

        when(customShoppingListItemService.bulkDelete(ids)).thenReturn(List.of(1L, 2L, 3L));

        mockMvc.perform(delete(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID)
            .param("ids", ids)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0]").value(1))
            .andExpect(jsonPath("$[1]").value(2))
            .andExpect(jsonPath("$[2]").value(3));

        verify(customShoppingListItemService).bulkDelete(ids);
    }

    @Test
    @DisplayName("Delete custom shopping list items without ids → 400 Bad Request")
    void bulkDelete_missingIds_badRequest() throws Exception {
        mockMvc.perform(delete(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(customShoppingListItemService);
    }

    @Test
    @DisplayName("Get all custom shopping items by status with no status → 200 OK")
    void getByStatus_noStatus_ok() throws Exception {
        when(customShoppingListItemService.findAllUsersCustomShoppingListItemsByStatus(USER_ID, null))
                .thenReturn(List.of(getCustomShoppingListItemResponseDto()));

        mockMvc.perform(get(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("text"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(customShoppingListItemService).findAllUsersCustomShoppingListItemsByStatus(USER_ID, null);
    }

    @Test
    @DisplayName("Get all custom shopping items by status with status active → 200 OK")
    void getByStatus_active_ok() throws Exception {
        String status = "ACTIVE";
        when(customShoppingListItemService.findAllUsersCustomShoppingListItemsByStatus(USER_ID, status))
            .thenReturn(List.of(getCustomShoppingListItemResponseDto()));

        mockMvc.perform(get(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID)
            .param("status", status)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].text").value("text"))
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(customShoppingListItemService).findAllUsersCustomShoppingListItemsByStatus(USER_ID, status);
    }

    @ParameterizedTest(name = "Get all custom shopping items by status with status \"{0}\" → 200 OK")
    @ValueSource(strings = {"ACTIVE", "DONE", "DISABLED", "INPROGRESS"})
    void getByStatus_various_ok(String status) throws Exception {
        CustomShoppingListItemResponseDto dto = CustomShoppingListItemResponseDto.builder()
            .id(1L)
            .status(ShoppingListItemStatus.valueOf(status))
            .text("text")
            .build();

        when(customShoppingListItemService.findAllUsersCustomShoppingListItemsByStatus(USER_ID, status))
            .thenReturn(List.of(dto));

        mockMvc.perform(get(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID)
            .param("status", status)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].text").value("text"))
            .andExpect(jsonPath("$[0].status").value(status));

        verify(customShoppingListItemService).findAllUsersCustomShoppingListItemsByStatus(USER_ID, status);
        clearInvocations(customShoppingListItemService);
    }

    @Test
    @DisplayName("Get all custom shopping items by status with status done when no such items found → 200 OK with empty list")
    void getByStatus_done_empty_ok() throws Exception {
        String status = "DONE";
        when(customShoppingListItemService.findAllUsersCustomShoppingListItemsByStatus(USER_ID, status))
            .thenReturn(List.of());

        mockMvc.perform(get(customShoppingListItemControllerLink + "/{userId}/custom-shopping-list-items", USER_ID)
            .param("status", status)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        verify(customShoppingListItemService).findAllUsersCustomShoppingListItemsByStatus(USER_ID, status);
    }
}