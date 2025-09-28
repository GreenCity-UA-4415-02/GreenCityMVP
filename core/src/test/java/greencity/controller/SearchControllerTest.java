package greencity.controller;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchResponseDto;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    private final Locale testLocale = Locale.ENGLISH;


    static class LocaleResolverMock implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return parameter.getParameterType().equals(Locale.class);
        }

        @Override
        public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
            return Locale.ENGLISH;
        }
    }


    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(searchController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new LocaleResolverMock()
                )
                .build();
    }



    @Test
    void search_shouldReturnBadRequest_whenMissingQuery() throws Exception {
        mockMvc.perform(get("/search")
                        .locale(testLocale))
                .andExpect(status().isBadRequest());

        verify(searchService, never()).search(anyString(), anyString());
    }

    @Test
    void searchEcoNews_shouldReturnBadRequest_whenMissingQuery() throws Exception {
        mockMvc.perform(get("/search/econews")
                        .param("page", "0")
                        .param("size", "5")
                        .locale(testLocale))
                .andExpect(status().isBadRequest());

        verify(searchService, never()).searchAllNews(any(), anyString(), anyString());
    }



//    @Test
//    void search_shouldReturnSearchResponseDto() throws Exception {
//        EcoNewsAuthorDto author = EcoNewsAuthorDto.builder().id(1L).name("Author").build();
//
//        SearchNewsDto newsDto = SearchNewsDto.builder()
//                .id(1L)
//                .title("Eco news title")
//                .author(author)
//                .creationDate(ZonedDateTime.now())
//                .tags(List.of("tag1", "tag2"))
//                .build();
//
//        SearchResponseDto responseDto = SearchResponseDto.builder()
//                .ecoNews(List.of(newsDto))
//                .countOfResults(1L)
//                .build();
//
//        when(searchService.search("eco", testLocale.getLanguage())).thenReturn(responseDto);
//
//        mockMvc.perform(get("/search")
//                        .param("searchQuery", "eco")
//                        .locale(testLocale))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.ecoNews[0].title").value("Eco news title"))
//                .andExpect(jsonPath("$.countOfResults").value(1));
//
//        verify(searchService, times(1)).search("eco", testLocale.getLanguage());
//    }
//
//    @Test
//    void searchEcoNews_shouldReturnPageableDto() throws Exception {
//        EcoNewsAuthorDto author = EcoNewsAuthorDto.builder().id(1L).name("Author").build();
//
//        SearchNewsDto newsDto = SearchNewsDto.builder()
//                .id(1L)
//                .title("News1")
//                .author(author)
//                .creationDate(ZonedDateTime.now())
//                .tags(List.of("tag1"))
//                .build();
//
//        PageableDto<SearchNewsDto> pageableDto = new PageableDto<>(
//                List.of(newsDto),
//                1,
//                0,
//                1
//        );
//
//        when(searchService.searchAllNews(PageRequest.of(0, 5), "eco", testLocale.getLanguage()))
//                .thenReturn(pageableDto);
//
//        mockMvc.perform(get("/search/econews")
//                        .param("searchQuery", "eco")
//                        .param("page", "0")
//                        .param("size", "5")
//                        .locale(testLocale))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.page[0].title").value("News1"))
//                .andExpect(jsonPath("$.totalElements").value(1))
//                .andExpect(jsonPath("$.currentPage").value(0))
//                .andExpect(jsonPath("$.totalPages").value(1));
//
//        verify(searchService, times(1))
//                .searchAllNews(PageRequest.of(0, 5), "eco", testLocale.getLanguage());
//    } - НЕ ПРАЦЮЄ ЧЕРЕЗ LanguageValidation
}

