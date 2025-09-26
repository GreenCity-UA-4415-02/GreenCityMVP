package greencity.controller;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchResponseDto;
import greencity.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SearchController.class)
@ContextConfiguration(classes = {SearchControllerTest.TestConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public SearchService searchService() {
            return Mockito.mock(SearchService.class);
        }

        @Bean
        public SearchController searchController(SearchService searchService) {
            return new SearchController(searchService);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SearchService searchService;

    private SearchResponseDto searchResponseDto;
    private PageableDto<SearchNewsDto> pageableDto;

    @BeforeEach
    void setUp() {
        SearchNewsDto newsDto = SearchNewsDto.builder()
                .id(1L)
                .title("Eco news title")
                .author(null)
                .creationDate(ZonedDateTime.now())
                .tags(List.of("tag1", "tag2"))
                .build();

        searchResponseDto = SearchResponseDto.builder()
                .ecoNews(List.of(newsDto))
                .countOfResults(1L)
                .build();

        pageableDto = new PageableDto<>(List.of(newsDto), 1L, 0, 1);
    }



    @Test
    void search_ShouldReturnBadRequest_WhenSearchQueryMissing() throws Exception {
        mockMvc.perform(get("/search")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchEcoNews_ShouldReturnBadRequest_WhenSearchQueryMissing() throws Exception {
        mockMvc.perform(get("/search/econews")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
