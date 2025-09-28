package greencity.controller;

import greencity.dto.user.UserVO;
import greencity.service.HabitStatisticService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class HabitStatisticControllerTest {

    @Mock
    private HabitStatisticService habitStatisticService;

    @InjectMocks
    private HabitStatisticController habitStatisticController;
    private MockMvc mockMvc;

    private static final String HABIT_LINK = "/habit/statistic";
    private static final Long HABIT_ID = 102L;
    private static final UserVO TEST_USER_VO;
    static {
        TEST_USER_VO = new UserVO();
        TEST_USER_VO.setId(1L);
        TEST_USER_VO.setEmail("test@gmail.com");
    }


    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitStatisticController)
                                      .setControllerAdvice()
                                      .setCustomArgumentResolvers(
                                              new PageableHandlerMethodArgumentResolver())
                                      .build();
    }

    @Test
    @DisplayName("Test findAllByHabitId should return 200 OK")
    void findAllByHabitIdStatus200() {
    }

    @Test
    @DisplayName("Test findAllStatsByHabitAssignId should return 200 OK")
    void findAllStatsByHabitAssignIdStatus200() {
    }

    @Test
    @DisplayName("Test saveHabitStatistic should return 200 OK")
    void saveHabitStatisticStatus200() {
    }

    @Test
    @DisplayName("Test updateStatistic should return 200 OK")
    void updateStatisticStatus200() {
    }

    @Test
    @DisplayName("Test getTodayStatisticsForAllHabitItems should return 200 OK")
    void getTodayStatisticsForAllHabitItemsStatus200() {
    }

    @Test
    @DisplayName("Test findAmountOfAcquiredHabits should return 200 OK")
    void findAmountOfAcquiredHabitsStatus200() {
    }

    @Test
    @DisplayName("Test findAmountOfHabitsInProgress should return 200 OK")
    void findAmountOfHabitsInProgressStatus200() {
    }
}